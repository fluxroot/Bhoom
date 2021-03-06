package sc.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import sc.engine.SearchEngine;
import sc.engine.engines.IDAspWinEngine;
import sc.engine.engines.MTDFBinaryEngine;
import sc.engine.engines.MTDFEngine;
import sc.engine.engines.MTDFHybridEngine;
import sc.testing.TestingUtils.EngineSetting;
import sc.testing.TestingUtils.SuiteResult;
import sc.util.ObjectPool.Factory;
import sc.util.UCI.EFactory;

public class SaveBenchmark {

	public static File dir = new File("testing");
	public static File benchmark = new File(dir, "benchmarks");
	public static File suites = new File(dir, "suites");

	private void saveBenchmark(String suiteFile,
			Factory<SearchEngine> engineFactory, EngineSetting setting,
			String saveFile) throws Exception {
		SuiteResult sr = TestingUtils.getTestResults(suiteFile, engineFactory,
				setting);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				saveFile));
		oos.writeObject(sr);
		oos.close();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(8);

		// SearchEngine se = new NFullQuiescenceEngine("NFullQuiescence", new
		// SideToMoveEvaluator(), 1000);
		final EngineSetting setting = new EngineSetting();
		setting.depth = 7;
		setting.timeMs = 0;
		final File suiteFile = new File(suites, "Test20.EPD");
		// File saveFile = new File(benchmark,
		// "SMALL-IDAsp-NullMoves-SeeWithHash-100sec.ser");
		// File saveFile = new File(benchmark,
		// "SMALL-MTDBinary-SeeWithHash-100sec.ser");
		// File saveFile = new File(benchmark,
		// "SMALL-MTDf-SeeWithHash-100sec.ser");

		for (Class<?> clz : new Class[] { /* IDAspWinEngine.class,
				MTDFEngine.class, MTDFBinaryEngine.class */ MTDFHybridEngine.class }) {
			final Class<?> c = clz;
			for (boolean lmr : new boolean[] { false, true }) {
				for (boolean nm : new boolean[] { false, true }) {
					for (boolean fp : new boolean[] { false, true }) {
						final EFactory ef = new EFactory(c, lmr, nm, fp);
						final String engineName = EFactory.getName(c, lmr, nm,
								fp);
						final String resultFileName = getResultFileName(
								setting, engineName, suiteFile);
						final File saveFile = new File(benchmark,
								resultFileName);

						// writeScript(ef.getName(c, lmr, nm, fp), c, lmr, nm,
						// fp);
						Runnable r = new Runnable() {
							public void run() {

								try {
									new SaveBenchmark()
											.saveBenchmark(
													suiteFile.getAbsolutePath(),
													ef, setting,
													saveFile.getAbsolutePath());
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};
						ses.execute(r);
					
					}
				}
			}

		}
		ses.awaitTermination(2, TimeUnit.HOURS);
		System.exit(0);
	}

	private static void writeScript(String name, Class<?> c, boolean lmr,
			boolean nm, boolean fp) {
		String scriptName = name + ".bat";
		File dir = new File("uciscripts");
		File of = new File(dir, scriptName);
		try {
			BufferedWriter fw = new BufferedWriter(new FileWriter(of));
			fw.write("java -Xmx1024M -cp bin sc.util.UCI ");
			String engineName = c.getName();
			int lastDot = engineName.lastIndexOf(".");
			fw.write(engineName.substring(lastDot + 1));
			fw.write(" ");
			fw.write(lmr ? "true" : "false");
			fw.write(" ");
			fw.write(nm ? "true" : "false");
			fw.write(" ");
			fw.write(fp ? "true" : "false");
			fw.newLine();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static private String getResultFileName(EngineSetting setting,
			String engineName, File suite) {
		String settingString = null;
		if (setting.depth >= 100) {
			settingString = "" + (setting.timeMs / 1000) + "sec.ser";
		} else {
			settingString = "depth" + setting.depth;
		}
		String fileName = suite.getName();
		int lastDot = fileName.lastIndexOf(".");
		return fileName.substring(0, lastDot) + "-" + engineName + "-"
				+ settingString;
	}

}
