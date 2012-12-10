package io.niowire.serversource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * This class acts as a server source by watching the configured directory and
 * parsing the files within it as JSON configuration files. On the first run it
 * will return all the files in this directory as being added. And from then on
 * it will return either added modified or deleted for each of the entries.
 *
 * @configparam directory {@link java.lang.String} - the directory that the
 * server configuration files are located
 *
 * @author Trent Houliston
 */
public class DirectoryServerSource implements NioServerSource
{

	private static final Logger LOG = LoggerFactory.getLogger(DirectoryServerSource.class);
	/**
	 * Our watcher for watching the directory
	 */
	private WatchService watcher;
	/**
	 * The path of our directory
	 */
	private Path path;
	/**
	 * A GSON instance to parse the json files
	 */
	private static Gson gson = new GsonBuilder().serializeNulls().create();
	/**
	 * First run so that it will return the files as being added
	 */
	private boolean firstRun = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(Map<String, Object> configuration) throws IOException
	{
		LOG.info("Starting up directory watching server source");
		//Open the watcher to catch the events
		watcher = FileSystems.getDefault().newWatchService();

		//Get our directory from the configuration
		String directory = (String) configuration.get("directory");

		//Get the path
		path = Paths.get(directory);

		//Register it with the watcher (note that this will throw an exception
		//if the directory does not exist)
		path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		watcher.close();
		watcher = null;
		LOG.info("Directory watching service closed");
	}

	/**
	 * Gets the changes to the servers since this method was last run. It will
	 * on first run get all the servers in the directory, and then on subsequent
	 * runs it will get the changes (servers that have been added modified or
	 * deleted).
	 *
	 * @return a map of NioServerConfiguration's to the event that occurred on
	 *            them (added deleted or modified)
	 *
	 * @throws IOException
	 */
	@Override
	public Map<NioServerDefinition, Event> getChanges() throws IOException
	{
		//If this is our first run
		if (firstRun)
		{
			//Not anymore
			firstRun = false;

			//Make a hashmap to store the results
			HashMap<NioServerDefinition, Event> map = new HashMap<NioServerDefinition, Event>();

			//Loop through all the files in our path
			for (Path file : Files.newDirectoryStream(path))
			{
				try
				{
					//Read the files and parse them form json
					BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset());
					NioServerDefinition config = gson.fromJson(reader, NioServerDefinition.class);

					//Put them in the map with the state of add
					LOG.info("Server definition {} was added", file.getFileName());
					map.put(config, Event.SERVER_ADD);
				}
				catch (Exception ex)
				{
					//Warn that the server definition is bad
					LOG.warn("Server definition {} is invalid and is being ignored", file.getFileName());
				}
			}

			//Return the map
			return map;
		}
		//Otherwise we are doing a normal check
		else
		{
			//Poll the watcher for changes
			WatchKey key = watcher.poll();

			//If there are some
			if (key != null)
			{
				HashMap<NioServerDefinition, Event> map = new HashMap<NioServerDefinition, Event>();

				//Loop through all the events which have occured since we last checked
				for (WatchEvent<?> event : key.pollEvents())
				{
					try
					{
						//Get the event
						WatchEvent<Path> e = (WatchEvent<Path>) event;

						//Get the actual path to the file
						Path file = path.resolve(e.context());

						//If it is an existing server that has been deleted we are removing the server
						if (e.kind() == ENTRY_DELETE)
						{
							NioServerDefinition config = new NioServerDefinition();
							config.setId(e.context().getFileName().toString());

							LOG.info("Server definition {} was removed", file.getFileName());
							map.put(config, Event.SERVER_REMOVE);
						}
						else
						{
							//Read the config file
							BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset());
							NioServerDefinition config = gson.fromJson(reader, NioServerDefinition.class);
							config.setId(file.getFileName().toString());

							//If its a new file then we are adding a server
							if (e.kind() == ENTRY_CREATE)
							{
								LOG.info("Server definition {} was added", file.getFileName());
								map.put(config, Event.SERVER_ADD);
							}
							//If it is an existing server that has been modified then we are updating a server
							else if (e.kind() == ENTRY_MODIFY)
							{
								LOG.info("Server definition {} was modified", file.getFileName());
								map.put(config, Event.SERVER_UPDATE);
							}
						}
					}
					catch (Exception ex)
					{
						LOG.warn("The server definition {} is invalid, ignoring (existing servers are not removed by this)", ((Path) event.context()).getFileName());
					}
				}

				return map;
			}
			else
			{
				//Don't return null for collections
				return Collections.EMPTY_MAP;
			}
		}
	}
}
