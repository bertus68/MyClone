package a.polverini.my;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.text.method.*;
import java.io.*;
import android.text.*;
import android.view.*;
import java.sql.*;
import java.util.*;
import android.graphics.*;
import android.text.style.*;
import a.polverini.my.MainActivity.*;
import android.view.inputmethod.*;
import android.webkit.*;
import android.content.*;

public class MainActivity extends Activity 
{
	public static boolean VERBOSE = true;
	public static String  H2URL = H2.FILE("/storage/emulated/0/tmp/sql/egscc-test");
	public static String  PGURL = PG.URL("egscc-specification", "82.95.110.59", 5432);
	
	private static Properties configuration = new Properties();

	private WebApp webapp;
	
	static {
		configuration.put("h2url", H2URL);
		configuration.put("pgurl", PGURL);
		configuration.put("verbose", VERBOSE);
	}
	
	public class WebApp {
		
		private final Context context;
		private WebView view;
		
		public WebApp(Context context, WebView view) {
			this.context = context;
			this.view = view;
			view.getSettings().setJavaScriptEnabled(true);
			view.addJavascriptInterface(this, "my");
			view.setWebViewClient(new WebViewClient() {
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {
						view.loadUrl(url);
						return true;
					}
				});
			view.evaluateJavascript("javascript: my.println(\"Hello!\");", null);
		}

		public void evaluate(String s) {
			view.evaluateJavascript("javascript: " +s, new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String s) {
						if(s==null) return;
						System.out.println(" < "+s);
					}
				});
		}

		@JavascriptInterface
		public void toast(String s) {
			Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
		}

		@JavascriptInterface
		public void println(String s) {
			System.out.println(s);
		}
		
		@JavascriptInterface
		public void set(String key, Object val) {
			configuration.put(key, val);
			switch(key) {
				case "verbose":
					VERBOSE = val;
					break;
				case "pgurl":
					PGURL = (String) val;
					break;
				case "h2url":
					H2URL = (String) val;
					break;
				default:
					break;
			}
		}

		@JavascriptInterface
		public Object get(String key) {
			return configuration.get(key);
		}
		
	}
	
    @Override
    protected void onCreate(Bundle saved)
    {
        super.onCreate(saved);
        setContentView(R.layout.main);
		
		new TextHandler((TextView)findViewById(R.id.text));
		System.out.println("MyClone rc-0.1.1");
		
		webapp = new WebApp(this, (WebView)findViewById(R.id.web));
		
		EditText edit = findViewById(R.id.edit);
		edit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView view, int id, KeyEvent event)
				{
					switch(id) {
					case EditorInfo.IME_NULL:
						if(event.getAction()==KeyEvent.ACTION_DOWN) {
							String s = view.getText().toString();
							System.out.println(" > "+s);
							view.setText("");
							webapp.evaluate(s);
							return true;
						}
						break;
					default:
						break;
					}
					return false;
				}
		});
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	boolean first = true;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
				case R.id.drop:
					new Drop().execute();
					return true;
				case R.id.init:
					new Init().execute();
					return true;
				case R.id.clean:
					new Clean().execute();
					return true;
				case R.id.copy:
					new Copy().execute();
					return true;
				case R.id.query:
					new Query().execute();
					return true;
				default:
					return super.onOptionsItemSelected(item);
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return true;
	}

	public static class Drop extends AsyncTask
	{
		@Override
		protected Object doInBackground(Object[] args)
		{
			long t0 = System.currentTimeMillis();

			PGS pgs = new PGS(PGURL, "apolverini", "Sabr1na$");
			pgs.connect();
			pgs.drop();
			pgs.disconnect();

			long dt = System.currentTimeMillis()-t0;
			System.out.printf("%5.3f seconds\n",dt/1000.0);
			return null;
		}
	}

	public static class Init extends AsyncTask
	{
		@Override
		protected Object doInBackground(Object[] args)
		{
			long t0 = System.currentTimeMillis();

			PGS pgs = new PGS(PGURL, "apolverini", "Sabr1na$");
			pgs.connect();
			pgs.init();
			pgs.disconnect();

			long dt = System.currentTimeMillis()-t0;
			System.out.printf("%5.3f seconds\n",dt/1000.0);
			return null;
		}
	}

	public static class Clean extends AsyncTask
	{
		@Override
		protected Object doInBackground(Object[] args)
		{
			long t0 = System.currentTimeMillis();

			PGS pgs = new PGS(PGURL, "apolverini", "Sabr1na$");
			pgs.connect();
			pgs.clean();
			pgs.disconnect();

			long dt = System.currentTimeMillis()-t0;
			System.out.printf("%5.3f seconds\n",dt/1000.0);
			return null;
		}
	}

	public static class Copy extends AsyncTask
	{
		@Override
		protected Object doInBackground(Object[] args)
		{
			long t0 = System.currentTimeMillis();

			H2S h2s = new H2S(H2URL);
			PGS pgs = new PGS(PGURL, "apolverini", "Sabr1na$");

			h2s.connect();
			pgs.connect();

			pgs.copy(h2s);

			pgs.disconnect();
			h2s.disconnect();

			long dt = System.currentTimeMillis()-t0;
			System.out.printf("%5.3f seconds\n",dt/1000.0);
			return null;
		}
	}

	public static class Query extends AsyncTask
	{
		@Override
		protected Object doInBackground(Object[] args)
		{
			try { 
				long t0 = System.currentTimeMillis();
				System.out.println("url="+PGURL);
				PGS pgs = new PGS(PGURL, "apolverini", "Sabr1na$");
				pgs.connect();
				pgs.query();
				pgs.disconnect();
				long dt = System.currentTimeMillis()-t0;
				System.out.printf("%5.3f seconds\n",dt/1000.0);
			} catch(Exception e) {
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
			return null;
		}
	}

	public static class PGS extends PG
	{
		public static final String TABLE_PROCEDURE_TESTCASE = "PROCEDURE_TEST_CASE";
		public static final String TABLE_MANUAL_PROCEDURE_STEP = "MANUAL_PROCEDURE_STEP";
		public static final String TABLE_MANUAL_PROCEDURE = "MANUAL_PROCEDURE";
		public static final String TABLE_AUTOMATED_PROCEDURE = "AUTOMATED_PROCEDURE";
		public static final String TABLE_AUXILIARY_ROUTINE = "AUXILIARYROUTINE";
		public static final String TABLE_PROCEDURE = "PROCEDURE";
		public static final String TABLE_SCENARIO_ADDITIONAL_INFORMATION = "SCENARIO_ADDITIONAL_INFORMATION";
		public static final String TABLE_SCENARIO_PERFORMANCE_MEASUREMENT = "SCENARIO_PERFORMANCE_MEASUREMENT";
		public static final String TABLE_SCENARIO_DEPLOYMENT = "SCENARIO_DEPLOYMENT";
		public static final String TABLE_SCENARIO = "SCENARIO";
		public static final String TABLE_TESTCASE_PROJECT_REQUIREMENT = "TEST_CASE_PROJECT_REQUIREMENT";
		public static final String TABLE_TESTCASE = "TEST_CASE";
		public static final String TABLE_FEATURE = "FEATURE";
		public static final String TABLE_TESTAREA = "TEST_AREA";
		public static final String TABLE_PERFORMANCE_MEASUREMENT = "PERFORMANCE_MEASUREMENT";
		public static final String TABLE_PROJECT_REQUIREMENT_DEPLOYMENT = "PROJECT_REQUIREMENT_DEPLOYMENT";
		public static final String TABLE_PROJECT_REQUIREMENT = "PROJECT_REQUIREMENT";
		public static final String TABLE_PROJECT = "PROJECT";
		public static final String TABLE_ADDITIONAL_INFORMATION = "ADDITIONAL_INFORMATION";
		public static final String TABLE_SOFTWARE_REQUIREMENT_USER_REQUIREMENT = "SOFTWARE_REQUIREMENT_USER_REQUIREMENT";
		public static final String TABLE_SOFTWARE_REQUIREMENT = "SOFTWARE_REQUIREMENT";
		public static final String TABLE_USER_REQUIREMENT = "USER_REQUIREMENT";
		public static final String TABLE_REQUIREMENT_DEPLOYMENT = "REQUIREMENT_DEPLOYMENT";
		public static final String TABLE_REQUIREMENT = "REQUIREMENT";
		public static final String TABLE_DEPLOYMENT = "DEPLOYMENT";
		public static final String TABLE_BASELINE_ITEM = "BASELINE_ITEM";
		public static final String TABLE_BASELINE = "BASELINE";
		public static final String TABLE_EDITING_LOCK = "EDITING_LOCK";

		public static String[] TABLES = new String[] {
			TABLE_EDITING_LOCK,
			TABLE_BASELINE,
			TABLE_BASELINE_ITEM, 
			TABLE_DEPLOYMENT, 
			TABLE_REQUIREMENT, 
			TABLE_REQUIREMENT_DEPLOYMENT, 
			TABLE_USER_REQUIREMENT, 
			TABLE_SOFTWARE_REQUIREMENT, 
			TABLE_SOFTWARE_REQUIREMENT_USER_REQUIREMENT, 
			TABLE_ADDITIONAL_INFORMATION, 
			TABLE_PROJECT, 
			TABLE_PROJECT_REQUIREMENT, 
			TABLE_PROJECT_REQUIREMENT_DEPLOYMENT, 
			TABLE_PERFORMANCE_MEASUREMENT, 
			TABLE_TESTAREA, 
			TABLE_FEATURE, 
			TABLE_TESTCASE, 
			TABLE_TESTCASE_PROJECT_REQUIREMENT, 
			TABLE_SCENARIO, 
			TABLE_SCENARIO_DEPLOYMENT, 
			TABLE_SCENARIO_PERFORMANCE_MEASUREMENT, 
			TABLE_SCENARIO_ADDITIONAL_INFORMATION, 
			TABLE_PROCEDURE, 
			TABLE_AUXILIARY_ROUTINE, 
			TABLE_AUTOMATED_PROCEDURE, 
			TABLE_MANUAL_PROCEDURE, 
			TABLE_MANUAL_PROCEDURE_STEP, 
			TABLE_PROCEDURE_TESTCASE
		};

		public static String DROP(String table) {
			return String.format("DROP TABLE IF EXISTS %s",table);
		}

		public static String DELETE(String table) {
			return String.format("DELETE FROM %s",table);
		}

		public static String CREATE(String table) {
			String sql = null;
			switch(table) {
				case TABLE_EDITING_LOCK:
					sql = "CREATE TABLE IF NOT EXISTS EDITING_LOCK("
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY,"
						+ " ID BIGINT NOT NULL,"
						+ " OWNER VARCHAR(255) NOT NULL,"
						+ " TYPE VARCHAR(255) NOT NULL"
						+ ");";
					break;
				case TABLE_BASELINE:
					sql = "CREATE TABLE IF NOT EXISTS BASELINE("
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY,"
						+ " DESCRIPTION TEXT,"
						+ " NAME VARCHAR(255) NOT NULL UNIQUE"
						+ " );";
					break;
				case TABLE_BASELINE_ITEM:
					sql = "CREATE TABLE IF NOT EXISTS BASELINE_ITEM("
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY,"
						+ " ID VARCHAR(255) NOT NULL UNIQUE,"
						+ " VERSION VARCHAR(255),"
						+ " BASELINE_PK BIGINT NOT NULL, "
						+ " FOREIGN KEY(BASELINE_PK) REFERENCES BASELINE (PK)"
						+ " );";
					break;
				case TABLE_DEPLOYMENT:
					sql = "CREATE TABLE IF NOT EXISTS PUBLIC.DEPLOYMENT("
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY ,"
						+ " DESCRIPTION VARCHAR(255),"
						+ " NAME VARCHAR(255) NOT NULL UNIQUE,"
						+ " PERFMEASUREMENTONLY VARCHAR(255) DEFAULT 'false'"
						+ ");";
					break;
				case TABLE_REQUIREMENT:
					sql = "CREATE TABLE IF NOT EXISTS REQUIREMENT("
						+ " REQUIREMENT_TYPE VARCHAR(31) NOT NULL,"
						+ " ID VARCHAR(255) NOT NULL UNIQUE PRIMARY KEY ,"
						+ " DESCRIPTION TEXT,"
						+ " IMPORTDATE TIMESTAMP,"
						+ " IMPORTFILE TEXT,"
						+ " NAME VARCHAR(255),"
						+ " PRIORITY VARCHAR(255),"
						+ " VERIFICATION VARCHAR(255),"
						+ " VERSION VARCHAR(255)"
						+ ");";
					break;
				case TABLE_REQUIREMENT_DEPLOYMENT:
					sql = "CREATE TABLE IF NOT EXISTS REQUIREMENT_DEPLOYMENT("
						+ " REQUIREMENT_ID VARCHAR(255) NOT NULL,"
						+ " DEPLOYMENTS_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY (REQUIREMENT_ID) REFERENCES REQUIREMENT (ID),"
						+ " FOREIGN KEY (DEPLOYMENTS_PK) REFERENCES DEPLOYMENT (PK)"
						+ " );";
					break;
				case TABLE_USER_REQUIREMENT:
					sql = "CREATE TABLE IF NOT EXISTS USER_REQUIREMENT("
						+ " ADDITIONALNOTE TEXT,"
						+ " JUSTIFICATION TEXT,"
						+ " LASTCHANGEDIN VARCHAR(255),"
						+ " REQUIREMENTLEVEL VARCHAR(255),"
						+ " REQUIREMENTTYPE VARCHAR(255),"
						+ " ID VARCHAR(255) NOT NULL UNIQUE PRIMARY KEY,"
						+ " FOREIGN KEY (ID) REFERENCES REQUIREMENT (ID)"
						+ " );";
					break;
				case TABLE_SOFTWARE_REQUIREMENT:
					sql = "CREATE TABLE IF NOT EXISTS SOFTWARE_REQUIREMENT("
						+ " COMMENT TEXT,"
						+ " STABILITY VARCHAR(255),"
						+ " ID VARCHAR(255) NOT NULL UNIQUE PRIMARY KEY,"
						+ " STRUCTURE VARCHAR(255),"
						+ " FOREIGN KEY (ID) REFERENCES REQUIREMENT (ID)"
						+ " );";
					break;
				case TABLE_SOFTWARE_REQUIREMENT_USER_REQUIREMENT:
					sql = "CREATE TABLE IF NOT EXISTS SOFTWARE_REQUIREMENT_USER_REQUIREMENT("
						+ " SOFTWARE_REQUIREMENT_ID VARCHAR(255) NOT NULL,"
						+ " USERREQUIREMENTS_ID VARCHAR(255) NOT NULL,"
						+ " FOREIGN KEY (SOFTWARE_REQUIREMENT_ID) REFERENCES SOFTWARE_REQUIREMENT (ID),"
						+ " FOREIGN KEY (USERREQUIREMENTS_ID) REFERENCES USER_REQUIREMENT (ID)"
						+ " );"; 
					break;
				case TABLE_ADDITIONAL_INFORMATION:
					sql = "CREATE TABLE IF NOT EXISTS ADDITIONAL_INFORMATION("
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY,"
						+ " DESCRIPTION TEXT,"
						+ " KEY VARCHAR(255) NOT NULL UNIQUE"
						+ " );";
					break;
				case TABLE_PROJECT:
					sql = "CREATE TABLE IF NOT EXISTS PROJECT("
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY,"
						+ " ARTIFACTID VARCHAR(255),"
						+ " BASEFOLDER TEXT,"
						+ " CODEGENERATIONTARGETFOLDER TEXT,"
						+ " ID VARCHAR(255) NOT NULL UNIQUE,"
						+ " PACKAGENAME VARCHAR(255),"
						+ " PROJECTTYPE VARCHAR(255),"
						+ " PARENT_PK BIGINT,"
						+ " VERSION VARCHAR(255)"
						+ ");";
					break;
				case TABLE_PROJECT_REQUIREMENT:
					sql = "CREATE TABLE IF NOT EXISTS PROJECT_REQUIREMENT("
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY,"
						+ " IMPLEMENTATIONSTATUS VARCHAR(255),"
						+ " REQUESTFORWAIVER VARCHAR(255),"
						+ " VERIFICATIONSTAGE VARCHAR(255),"
						+ " PROJECT_PK BIGINT NOT NULL,"
						+ " REQUIREMENT_ID VARCHAR(255) NOT NULL,"
						+ " COMMENT TEXT,"
						+ " FOREIGN KEY (PROJECT_PK) REFERENCES PROJECT (PK),"
						+ " FOREIGN KEY (REQUIREMENT_ID) REFERENCES REQUIREMENT (ID)"
						+ ");";
					break;
				case TABLE_PROJECT_REQUIREMENT_DEPLOYMENT:
					sql = "CREATE TABLE IF NOT EXISTS PROJECT_REQUIREMENT_DEPLOYMENT("
						+ " PROJECT_REQUIREMENT_PK BIGINT NOT NULL,"
						+ " DEPLOYMENTS_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY (PROJECT_REQUIREMENT_PK) REFERENCES PROJECT_REQUIREMENT (PK),"
						+ " FOREIGN KEY (DEPLOYMENTS_PK) REFERENCES DEPLOYMENT (PK)"
						+ ");";
					break;
				case TABLE_PERFORMANCE_MEASUREMENT:
					sql = "CREATE TABLE IF NOT EXISTS PERFORMANCE_MEASUREMENT("
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY,"
						+ " BASEVALUE DOUBLE PRECISION,"
						+ " DESCRIPTION TEXT,"
						+ " KEY VARCHAR(255) NOT NULL,"
						+ " TARGETVALUE DOUBLE PRECISION,"
						+ " PROJECT_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY (PROJECT_PK) REFERENCES PROJECT (PK)"
						+ ");";
					break;
				case TABLE_TESTAREA:
					sql = "CREATE TABLE IF NOT EXISTS TEST_AREA("
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY,"
						+ " APPROACH TEXT,"
						+ " DESCRIPTION TEXT,"
						+ " ID VARCHAR(255) NOT NULL,"
						+ " TITLE VARCHAR(255),"
						+ " PROJECT_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY(PROJECT_PK) REFERENCES PROJECT(PK)"
						+ " );";
					break;
				case TABLE_FEATURE:
					sql = "CREATE TABLE IF NOT EXISTS FEATURE("
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY,"
						+ " DESCRIPTION TEXT,"
						+ " ID VARCHAR(255) NOT NULL,"
						+ " TITLE VARCHAR(255),"
						+ " TESTAREA_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY (TESTAREA_PK) REFERENCES TEST_AREA (PK)"
						+ " );";
					break;
				case TABLE_TESTCASE:
					sql = "CREATE TABLE IF NOT EXISTS TEST_CASE("
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY,"
						+ " COMMENT TEXT,"
						+ " CRITERIA TEXT,"
						+ " ID VARCHAR(255) NOT NULL,"
						+ " SCOPE TEXT,"
						+ " SPECIFICATION TEXT,"
						+ " TITLE VARCHAR(255),"
						+ " FEATURE_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY(FEATURE_PK) REFERENCES FEATURE (PK)"
						+ ");";
					break;
				case TABLE_TESTCASE_PROJECT_REQUIREMENT:
					sql = "CREATE TABLE IF NOT EXISTS TEST_CASE_PROJECT_REQUIREMENT("
						+ " TEST_CASE_PK BIGINT NOT NULL,"
						+ " PROJECTREQUIREMENTS_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY (TEST_CASE_PK) REFERENCES TEST_CASE (PK),"
						+ " FOREIGN KEY (PROJECTREQUIREMENTS_PK) REFERENCES PROJECT_REQUIREMENT (PK)"
						+ " );";
					break;
				case TABLE_SCENARIO:
					sql = "CREATE TABLE IF NOT EXISTS SCENARIO("
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY,"
						+ " DESCRIPTION TEXT,"
						+ " ID VARCHAR(255) NOT NULL,"
						+ " RESOURCES TEXT,"
						+ " SCENARIOTYPE VARCHAR(255),"
						+ " TITLE VARCHAR(255),"
						+ " PROJECT_PK BIGINT NOT NULL,"
						+ " TESTAREA_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY (PROJECT_PK) REFERENCES PROJECT (PK),"
						+ " FOREIGN KEY (TESTAREA_PK) REFERENCES TEST_AREA (PK)"
						+ " );"; 
					break;
				case TABLE_SCENARIO_DEPLOYMENT:
					sql = "CREATE TABLE IF NOT EXISTS SCENARIO_DEPLOYMENT("
						+ " SCENARIO_PK BIGINT NOT NULL,"
						+ " DEPLOYMENTS_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY (SCENARIO_PK) REFERENCES SCENARIO (PK),"
						+ " FOREIGN KEY (DEPLOYMENTS_PK) REFERENCES DEPLOYMENT (PK)"
						+ " );";
					break;
				case TABLE_SCENARIO_PERFORMANCE_MEASUREMENT:
					sql = "CREATE TABLE IF NOT EXISTS SCENARIO_PERFORMANCE_MEASUREMENT("
						+ " SCENARIO_PK BIGINT NOT NULL,"
						+ " PERFORMANCEMEASUREMENTS_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY (SCENARIO_PK) REFERENCES SCENARIO (PK),"
						+ " FOREIGN KEY (PERFORMANCEMEASUREMENTS_PK) REFERENCES PERFORMANCE_MEASUREMENT (PK)"
						+ " );";
					break;
				case TABLE_SCENARIO_ADDITIONAL_INFORMATION:
					sql = "CREATE TABLE IF NOT EXISTS SCENARIO_ADDITIONAL_INFORMATION("
						+ " SCENARIO_PK BIGINT NOT NULL,"
						+ " ADDITIONALINFORMATION_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY(SCENARIO_PK) REFERENCES SCENARIO (PK),"
						+ " FOREIGN KEY(ADDITIONALINFORMATION_PK) REFERENCES ADDITIONAL_INFORMATION (PK)"
						+ " );";
					break;
				case TABLE_PROCEDURE:
					sql = "CREATE TABLE IF NOT EXISTS PROCEDURE("
						+ " PROCEDURE_TYPE VARCHAR(31) NOT NULL,"
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY,"
						+ " DESCRIPTION TEXT,"
						+ " ID VARCHAR(255) NOT NULL,"
						+ " TITLE VARCHAR(255),"
						+ " SCENARIO_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY (SCENARIO_PK) REFERENCES SCENARIO (PK)"
						+ " );";
					break;
				case TABLE_AUXILIARY_ROUTINE:
					sql = "CREATE TABLE IF NOT EXISTS AUXILIARYROUTINE("
						+ " PK BIGINT NOT NULL UNIQUE PRIMARY KEY,"
						+ " FOREIGN KEY (PK) REFERENCES PROCEDURE (PK)"
						+ " );";
					break;
				case TABLE_AUTOMATED_PROCEDURE:
					sql = "CREATE TABLE IF NOT EXISTS AUTOMATED_PROCEDURE("
						+ " PK BIGINT NOT NULL UNIQUE PRIMARY KEY,"
						+ " FOREIGN KEY (PK) REFERENCES PROCEDURE (PK)"
						+ " );";
					break;
				case TABLE_MANUAL_PROCEDURE:
					sql = "CREATE TABLE IF NOT EXISTS MANUAL_PROCEDURE("
						+ " PK BIGINT NOT NULL UNIQUE PRIMARY KEY,"
						+ " FOREIGN KEY (PK) REFERENCES PROCEDURE (PK)"
						+ " );";
					break;
				case TABLE_MANUAL_PROCEDURE_STEP:
					sql = "CREATE TABLE IF NOT EXISTS MANUAL_PROCEDURE_STEP("
						+ " PK BIGSERIAL NOT NULL PRIMARY KEY,"
						+ " ACTION VARCHAR(255),"
						+ " COMMENTS TEXT,"
						+ " EXPECTEDRESULTS VARCHAR(255),"
						+ " STEPNUMBER INTEGER,"
						+ " MANUALPROCEDURE_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY (MANUALPROCEDURE_PK) REFERENCES MANUAL_PROCEDURE (PK)"
						+ ");";
					break;
				case TABLE_PROCEDURE_TESTCASE:
					sql = "CREATE TABLE IF NOT EXISTS PROCEDURE_TEST_CASE("
						+ " PROCEDURE_PK BIGINT NOT NULL,"
						+ " TESTCASES_PK BIGINT NOT NULL,"
						+ " FOREIGN KEY (PROCEDURE_PK) REFERENCES PROCEDURE (PK),"
						+ " FOREIGN KEY (TESTCASES_PK) REFERENCES TEST_CASE (PK)"
						+ " );";
					break;
			}
			return sql;
		}

		public PGS(String url, String user, String pswd) {
			super(url, user, pswd);
		}

		public PGS(String url) {
			super(url);
		}

		public void drop() {
			for(int i=TABLES.length-1; i>=0; i--) {
				exec(DROP(TABLES[i]));
			}
		}

		public void init() {
			for(int i=0; i<TABLES.length; i++) {
				exec(CREATE(TABLES[i]));
			}
		}

		public void clean() {
			for(int i=TABLES.length-1; i>=0; i--) {
				exec(DELETE(TABLES[i]));
			}
		}

		public void copy(DB db) {
			try {
				getConnection().setAutoCommit(false);
				for(int i=1; i<TABLES.length; i++) {
					copy(TABLES[i], db);
				}
				getConnection().commit();
				getConnection().setAutoCommit(true);
			} catch(Exception e) {
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
		}

		public void query() {
			query(TABLE_PROJECT, new DB.QueryCallback() {
					@Override
					public void results(String table, Set<Properties> rows) {
						for(Properties row : rows) {
							System.out.printf("project: %s\n", row.get("id") );
						}
					}
				});
			query(TABLE_TESTAREA, new DB.QueryCallback() {
					@Override
					public void results(String table, Set<Properties> rows) {
						for(Properties row : rows) {
							System.out.printf("testarea: %s\n", row.get("id") );
						}
					}
				});
		}

		public void queryall() {
			for(int i=0; i<TABLES.length; i++) {
				query(TABLES[i], new DB.QueryCallback() {
						@Override
						public void results(String table, Set<Properties> rows)
						{
							// TODO: Implement this method
						}
					});
			}
		}
	}

	public static class H2S extends H2
	{
		public static final String TABLE_PROCEDURE_TESTCASE = "PROCEDURE_TEST_CASE";
		public static final String TABLE_MANUAL_PROCEDURE_STEP = "MANUAL_PROCEDURE_STEP";
		public static final String TABLE_MANUAL_PROCEDURE = "MANUAL_PROCEDURE";
		public static final String TABLE_AUTOMATED_PROCEDURE = "AUTOMATED_PROCEDURE";
		public static final String TABLE_AUXILIARY_ROUTINE = "AUXILIARYROUTINE";
		public static final String TABLE_PROCEDURE = "PROCEDURE";
		public static final String TABLE_SCENARIO_ADDITIONAL_INFORMATION = "SCENARIO_ADDITIONAL_INFORMATION";
		public static final String TABLE_SCENARIO_PERFORMANCE_MEASUREMENT = "SCENARIO_PERFORMANCE_MEASUREMENT";
		public static final String TABLE_SCENARIO_DEPLOYMENT = "SCENARIO_DEPLOYMENT";
		public static final String TABLE_SCENARIO = "SCENARIO";
		public static final String TABLE_TESTCASE_PROJECT_REQUIREMENT = "TEST_CASE_PROJECT_REQUIREMENT";
		public static final String TABLE_TESTCASE = "TEST_CASE";
		public static final String TABLE_FEATURE = "FEATURE";
		public static final String TABLE_TESTAREA = "TEST_AREA";
		public static final String TABLE_PERFORMANCE_MEASUREMENT = "PERFORMANCE_MEASUREMENT";
		public static final String TABLE_PROJECT_REQUIREMENT_DEPLOYMENT = "PROJECT_REQUIREMENT_DEPLOYMENT";
		public static final String TABLE_PROJECT_REQUIREMENT = "PROJECT_REQUIREMENT";
		public static final String TABLE_PROJECT = "PROJECT";
		public static final String TABLE_ADDITIONAL_INFORMATION = "ADDITIONAL_INFORMATION";
		public static final String TABLE_SOFTWARE_REQUIREMENT_USER_REQUIREMENT = "SOFTWARE_REQUIREMENT_USER_REQUIREMENT";
		public static final String TABLE_SOFTWARE_REQUIREMENT = "SOFTWARE_REQUIREMENT";
		public static final String TABLE_USER_REQUIREMENT = "USER_REQUIREMENT";
		public static final String TABLE_REQUIREMENT_DEPLOYMENT = "REQUIREMENT_DEPLOYMENT";
		public static final String TABLE_REQUIREMENT = "REQUIREMENT";
		public static final String TABLE_DEPLOYMENT = "DEPLOYMENT";
		public static final String TABLE_BASELINE_ITEM = "BASELINE_ITEM";
		public static final String TABLE_BASELINE = "BASELINE";

		public static String[] TABLES = new String[] {
			TABLE_BASELINE,
			TABLE_BASELINE_ITEM, 
			TABLE_DEPLOYMENT, 
			TABLE_REQUIREMENT, 
			TABLE_REQUIREMENT_DEPLOYMENT, 
			TABLE_USER_REQUIREMENT, 
			TABLE_SOFTWARE_REQUIREMENT, 
			TABLE_SOFTWARE_REQUIREMENT_USER_REQUIREMENT, 
			TABLE_ADDITIONAL_INFORMATION, 
			TABLE_PROJECT, 
			TABLE_PROJECT_REQUIREMENT, 
			TABLE_PROJECT_REQUIREMENT_DEPLOYMENT, 
			TABLE_PERFORMANCE_MEASUREMENT, 
			TABLE_TESTAREA, 
			TABLE_FEATURE, 
			TABLE_TESTCASE, 
			TABLE_TESTCASE_PROJECT_REQUIREMENT, 
			TABLE_SCENARIO, 
			TABLE_SCENARIO_DEPLOYMENT, 
			TABLE_SCENARIO_PERFORMANCE_MEASUREMENT, 
			TABLE_SCENARIO_ADDITIONAL_INFORMATION, 
			TABLE_PROCEDURE, 
			TABLE_AUXILIARY_ROUTINE, 
			TABLE_AUTOMATED_PROCEDURE, 
			TABLE_MANUAL_PROCEDURE, 
			TABLE_MANUAL_PROCEDURE_STEP, 
			TABLE_PROCEDURE_TESTCASE
		};

		public static String DROP(String table) {
			return String.format("DROP TABLE IF EXISTS %s",table);
		}

		public static String DELETE(String table) {
			return String.format("DELETE FROM %s",table);
		}

		public static String CREATE(String table) {
			return null;
		}

		public H2S(String url) {
			super(url, "SA", "");
		}

		public void drop() {
			for(int i=TABLES.length-1; i>=0; i--) {
				exec(DROP(TABLES[i]));
			}
		}

		public void init() {
			for(int i=0; i<TABLES.length; i++) {
				exec(CREATE(TABLES[i]));
			}
		}

		public void clean() {
			for(int i=TABLES.length-1; i>=0; i--) {
				exec(DELETE(TABLES[i]));
			}
		}

		public void copy(DB db) {
			try {
				getConnection().setAutoCommit(false);
				for(int i=0; i<TABLES.length; i++) {
					copy(TABLES[i], db);
				}
				getConnection().commit();
				getConnection().setAutoCommit(true);
			} catch(Exception e) {
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
		}

		public void query() {
			for(int i=0; i<TABLES.length; i++) {
				query(TABLES[i], new DB.QueryCallback() {

						@Override
						public void results(String table, Set<Properties> rows)
						{
							// TODO: Implement this method
						}
					});
			}
		}

	}

	public static class PG extends DB
	{
		private static String HOST = "localhost";
		private static int PORT = 5432;

		static {
			try {
				Class.forName("org.postgresql.Driver");
			} catch (ClassNotFoundException e) {
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
		}

		public static String URL(String name, String host, int port) {
			return String.format("jdbc:postgresql://%s:%d/%s", host, port, name);
		}

		public static String URL(String name) {
			return String.format("jdbc:postgresql://%s:%d/%s", HOST, PORT, name);
		}

		public PG(String url, String user, String pswd) {
			super(url, user, pswd);
		}

		public PG(String url) {
			super(url);
		}

	}

	public static class H2 extends DB
	{
		static {
			try {
				Class.forName("org.h2.Driver");
			} catch (ClassNotFoundException e) {
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
		}

		public static String FILE(String path) {
			return String.format("jdbc:h2://%s", path);
		}

		public H2(String url, String user, String pswd) {
			super(url, user, pswd);
		}

		public H2(String url) {
			super(url, "SA", "");
		}

	}

	public static class DB
	{
		private final String url;
		private final String user;
		private final String pswd;

		private Connection connection;

		public Connection getConnection() {
			return connection;
		}

		public DB(String url, String user, String pswd) {
			this.url = url;
			this.user = user;
			this.pswd = pswd;
		}

		public DB(String url) {
			this.url = url;
			this.user = null;
			this.pswd = null;
		}

		public boolean isConnected() {
			try {
				return (connection!=null && !connection.isClosed());
			} catch(SQLException e){
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
			return false;
		}

		public void connect() {
			if(connection!=null) return;
			try {
				if(user!=null) {
					connection = DriverManager.getConnection(url, user, pswd);
				} else {
					connection = DriverManager.getConnection(url);
				}
			} catch(SQLException e){
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
		}

		public void disconnect() {
			if(!isConnected()) return;
			try {
				connection.close();
			} catch(SQLException e){
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
			connection = null;
		}

		public int count(String table) {
			if(!isConnected()) return 0;
			int count = 0;
			String sql = String.format("SELECT count(*) FROM %s", table);
			Statement statement = null;
			ResultSet rs = null;
			try {
				statement = connection.createStatement();
				rs = statement.executeQuery(sql);
				rs.next();
				count = rs.getInt(1);
			} catch (SQLException e) {
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
			close(rs);
			close(statement);
			return count;
		}

		public void exec(String sql) {
			if(!isConnected()) return;
			if(sql==null) return;
			if(VERBOSE) System.out.println(sql);
			Statement statement = null;
			try {
				statement = connection.createStatement();
				statement.execute(sql);
			} catch (SQLException e) {
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
			close(statement);
		}

		public void exec(List<String> list) {
			if(!isConnected()) return;
			if(list==null) return;
			Statement statement = null;
			try {
				statement = connection.createStatement();
				for(int i=0; i<list.size(); i++){
					String sql = list.get(i);
					if(sql!=null) {
						if(VERBOSE) System.out.println(sql);
						statement.addBatch(sql);
					}
					if(i%100==0 || i==list.size()) {
						statement.executeBatch();
					}
				}
			} catch (SQLException e) {
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
				while((e = e.getNextException())!=null) {
					System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
				}
			}
			close(statement);
		}

		private void close(ResultSet rs) {
			try {
				if(rs!=null) {
					rs.close();
				}
			} catch (SQLException e) {
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
		}

		private void close(Statement statement) {
			try {
				if(statement!=null) {
					statement.close();
				}
			} catch (SQLException e) {
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
		}

		interface QueryCallback {
			public void results(String table, Set<Properties> rows);
		}

		public void query(String table, QueryCallback callback)
		{
			if(!isConnected()) return;
			Statement statement = null;
			ResultSet rs = null;
			try {
				String query = String.format("SELECT * FROM %s", table);
				if(VERBOSE) System.out.println(query);
				statement = connection.createStatement();
				rs = statement.executeQuery(query);
				ResultSetMetaData metadata = rs.getMetaData();
				List<String> names = new ArrayList<>();
				for(int c=1; c<=metadata.getColumnCount(); c++){
					String name = metadata.getColumnName(c);
					if(VERBOSE) System.out.printf("%d) %s\n",c, name);
					names.add(name);
				}

				int n=0;
				Set<Properties> rows = new HashSet<>();
				while (rs.next()) {
					if(VERBOSE) System.out.printf("%s[%d]\n",table,n);
					Properties properties = new Properties();
					for(int c=1; c<=metadata.getColumnCount(); c++){
						Object val = rs.getObject(c);
						if(VERBOSE) System.out.printf("  %s=%s\n",names.get(c-1), val);
						if(val!=null) properties.put(names.get(c-1), val);
					}
					rows.add(properties);
					n++;
				}
				if(callback!=null) callback.results(table, rows);
			} catch (SQLException e) {
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
			close(rs);
			close(statement);
		}

		public void copy(String table, DB db)
		{
			if(!isConnected()) return;
			if(!db.isConnected()) return;

			PreparedStatement prepared = null;
			Statement statement = null;
			ResultSet rs = null;
			try {
				String query = String.format("SELECT * FROM %s", table);
				//if(VERBOSE) 
				System.out.println(query);
				statement = db.connection.createStatement();
				rs = statement.executeQuery(query);
				ResultSetMetaData metadata = rs.getMetaData();
				List<String> token = new ArrayList<>();
				List<String> names = new ArrayList<>();
				for(int c=1; c<=metadata.getColumnCount(); c++){
					names.add(metadata.getColumnName(c));
					token.add("?");
				}

				String insert = String.format("INSERT INTO %s (%s) VALUES (%s)", table, String.join(", ", names), String.join(", ", token));
				if(VERBOSE) System.out.println(insert);
				prepared = connection.prepareStatement(insert);
				int n=0;
				while (rs.next()) {
					for(int c=1; c<=metadata.getColumnCount(); c++){
						prepared.setObject(c, rs.getObject(c));
					}
					prepared.addBatch();
					n++;
					if(n%1000==0 || rs.isLast()) {
						prepared.executeBatch();
					}
				}
			} catch (SQLException e) {
				System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
				while((e = e.getNextException())!=null) {
					System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
				}
			}
			close(rs);
			close(prepared);
			close(statement);
		}
	}

	public static class TextHandler extends Handler {

		private static final int CLEAR = 100;
		private static final int PRINT = 101;
		private static final int ERROR = 102;

		private TextView text;

		public TextHandler(TextView text) {

			super(Looper.getMainLooper());
			this.text = text;
			this.text.setMovementMethod(new ScrollingMovementMethod());

			System.setOut(new PrintStream(System.out) {

					public void close() {
						obtainMessage(CLEAR).sendToTarget();
					}

					@Override
					public void println(String s) {
						obtainMessage(PRINT, s+"\n").sendToTarget();
					}

					@Override
					public PrintStream printf(String f, Object... o) {
						obtainMessage(PRINT, String.format(f, o)).sendToTarget();
						return this;
					}

				});

			System.setErr(new PrintStream(System.out) {

					public void close() {
						obtainMessage(CLEAR).sendToTarget();
					}

					@Override
					public void println(String s) {
						obtainMessage(ERROR, s+"\n").sendToTarget();
					}

					@Override
					public PrintStream printf(String f, Object... o) {
						obtainMessage(ERROR, String.format(f, o)).sendToTarget();
						return this;
					}

				});
		}

		@Override
		public void handleMessage(Message message) {
			switch(message.what) {
				case CLEAR:
					text.setText("");
					break;
				case PRINT:
					if(message.obj instanceof String) {
						text.append((String)message.obj);
						Layout layout = text.getLayout();
						if(layout!=null)  {
							int top = layout.getLineTop(text.getLineCount());
							int bottom = text.getBottom();
							if(top>bottom) {
								text.scrollTo(0, top-bottom);
							}
						}
					}
					break;
				case ERROR:
					if(message.obj instanceof String) {
						String s = (String)message.obj;
						text.append(s);
						((Spannable)text.getText()).setSpan(new ForegroundColorSpan(Color.RED), text.length()-s.length(), text.length(), 0);
						Layout layout = text.getLayout();
						if(layout!=null)  {
							int top = layout.getLineTop(text.getLineCount());
							int bottom = text.getBottom();
							if(top>bottom) {
								text.scrollTo(0, top-bottom);
							}
						}
					}
					break;
				default:
					break;
			}
		}
	}
}
