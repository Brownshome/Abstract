package abstractgame.io;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import abstractgame.io.user.Console;
import abstractgame.util.ApplicationException;
import abstractgame.util.ProcessFuture;
import abstractgame.util.Util;

public class FileIO {
	//This must be ordered, see ModelLoader#preLoadModel(String, ModelTask...)
	public static final ExecutorService IO_THREAD = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, 
			new LinkedBlockingQueue<Runnable>(), runnable -> new Thread(runnable, "IO-THREAD")) {
				@Override
				protected void afterExecute(Runnable r, Throwable t) {
					if(t == null && r instanceof FutureTask<?>) {
						try {
							((FutureTask) r).get();
						} catch(Throwable throwable) {
							t = throwable;
						}
					}
					
					if(t != null)
						if(t instanceof ApplicationException)
							throw (ApplicationException) t;
						else
							throw new ApplicationException("Exception in IO thread", t);
				}
			};

	/** Reads a whole file as lines, this method blocks until the read is 
	 * complete. Returns an empty list is the file does not exist and ignoreAbsence is true */
	public static List<String> readTextFile(Path path, boolean ignoreAbsence) throws IOException {
		if(!Files.exists(path) && ignoreAbsence)
			return Collections.EMPTY_LIST;
		
		return Files.readAllLines(path);
	}
	
	public static String readTextFileAsString(Path path, boolean ignoreAbsence) throws IOException {
		return Util.collect(FileIO.readTextFile(path, ignoreAbsence));
	}
	
	/** To be used for large files such as images */
	public static Future<MappedByteBuffer> readFileAsyncMapped(Path path, boolean ignoreAbsence) {
		Console.fine("Reading " + path.toAbsolutePath() + " async with using memory mapping.", "IO");
		
		return IO_THREAD.submit(() -> {
			try (FileChannel channel = FileChannel.open(path, READ)) {
				return channel.map(MapMode.READ_ONLY, 0, channel.size()).load();
			} catch (IOException e) {
				throw new ApplicationException("Unable to read file" + path, e, "IO");
			}
		});
	}
	
	public static Future<List<String>> readTextFileAsync(Path path, boolean ignoreAbsence) {
		Console.fine("Reading " + path.toAbsolutePath() + " async.", "IO");
		
		return IO_THREAD.submit(() -> {
			if(!ignoreAbsence || Files.isReadable(path))
				try {
					return Files.readAllLines(path);
				} catch (IOException e) {
					throw new ApplicationException("Unable to read file" + path, e, "IO");
				}
			
			return null;
		});
	}
	
	public static Future<String> readTextFileAsyncAsString(Path path, boolean ignoreAbsence) {
		return new ProcessFuture<>(readTextFileAsync(path, ignoreAbsence), Util::collect);
	}
	
	/** Shuts down the IO thread waiting for already running opperations to complete, if harsh all queued opperations that are not
	 * started are dropped. */
	public static void close(boolean harsh) {
		if(harsh)
			IO_THREAD.shutdownNow();
		else {
			IO_THREAD.shutdown();
			try {
				while(!IO_THREAD.awaitTermination(5, TimeUnit.SECONDS));
			} catch (InterruptedException e) {
				throw new ApplicationException(e, "IO");
			}
		}
	}

	public static Future<?> writeAsync(Path path, String data, OpenOption... openOptions) {
		return writeAsync(path, data.getBytes(StandardCharsets.UTF_8), openOptions);
	}
	
	public static Future<?> writeAsync(Path path, byte[] data, OpenOption... openOptions) {
		return IO_THREAD.submit(() -> {
			try {
				Files.write(path, data, openOptions);
			} catch (IOException e) {
				throw new ApplicationException("Could not write to file " + path, e, "IO");
			}
		});
	}

	public static Future<?> writeAsync(Path path, String data) {
		return writeAsync(path, data.getBytes(StandardCharsets.UTF_8), CREATE);
	}

	public static MappedByteBuffer readFileMapped(Path path, OpenOption... openOptions) {
		try (FileChannel channel = FileChannel.open(path, openOptions)) {
			return channel.map(MapMode.READ_ONLY, 0, channel.size());
		} catch (IOException e) {
			throw new ApplicationException("Unable to map file " + path, e, "IO");
		}
	}
}
