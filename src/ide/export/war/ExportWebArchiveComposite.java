package ide.export.war;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.environment.Environment;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.json.JSONObject;
import org.json.util.generic.JSONStringUtility;

public class ExportWebArchiveComposite extends Composite {

	JSONObject project_information;
	String build_file_data;
	String build_file_path;
	String deployment_dir;
	String application_name;
	private Text log;
	private Text txtFileName;
	private Text txtTargetDir;
	private Group grpLogMonitor;
	private CLabel lblNewLabel;
	private CLabel lblwar;
	private Composite composite;
	private CLabel lblOutputPath;
	private Button btnCancel;
	private Button btnBuild;
	private CLabel lblNewLabel_1;
	private boolean completed = false;
	
	public ExportWebArchiveComposite(Composite parent) {
		super(parent, SWT.NONE);
		project_information = Activator.getProjectInformation();
		deployment_dir = project_information.getString("project_full_path") + Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR) + "deploy";
		build_file_data = Environment.Resources.getStringFromResource("/ide/export/war/build.xml");
		build_file_path = project_information.getString("project_full_path") + Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR) + "build.xml";
		if(project_information.has("properties")) {
			if(project_information.getJSONObject("properties").has("projectDescription")) {
				if(project_information.getJSONObject("properties").getJSONObject("projectDescription").has("name")) {
					application_name = project_information.getJSONObject("properties").getJSONObject("projectDescription").getString("name").trim().replaceAll(" ", "_");
				}
			}
		}
		populate();
	}
	
	private void populate() {
		setLayout(new GridLayout(4, false));
		
		lblNewLabel_1 = new CLabel(this, SWT.NONE);
		lblNewLabel_1.setImage(new Image(null, ExportWebArchiveComposite.class.getResourceAsStream("/ide/export/war/war-big.png")));
		lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 3));
		lblNewLabel_1.setText("");
		
		lblNewLabel = new CLabel(this, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Archive Name:");
		
		txtFileName = new Text(this, SWT.BORDER | SWT.RIGHT);
		txtFileName.setText(application_name);
		txtFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		lblwar = new CLabel(this, SWT.NONE);
		lblwar.setText(".war");
		
		lblOutputPath = new CLabel(this, SWT.NONE);
		lblOutputPath.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblOutputPath.setText("Output Directory:");
		
		txtTargetDir = new Text(this, SWT.BORDER | SWT.RIGHT);
		txtTargetDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtTargetDir.setText(deployment_dir);
		
		Button btnBrowse = new Button(this, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				try {
					txtTargetDir.setText(dialog.open());
				} catch (Exception ignore) {}
			}
		});
		btnBrowse.setText("Browse");
		new CLabel(this, SWT.NONE);
		
		Button txtKeepBuildFile = new Button(this, SWT.CHECK);
		txtKeepBuildFile.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		txtKeepBuildFile.setSelection(true);
		txtKeepBuildFile.setText("Preserve ANT build file.");
		
		grpLogMonitor = new Group(this, SWT.NONE);
		grpLogMonitor.setText("Log monitor:");
		grpLogMonitor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		grpLogMonitor.setLayout(new GridLayout(1, false));
		
		log = new Text(grpLogMonitor, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		log.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		log.setEditable(false);
		log.setText("The application will be packaged with the following dependencies:" + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + project_information.getJSONArray("libraries").toString(1));
		
		composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
		composite.setLayout(new GridLayout(4, false));
		
		btnCancel = new Button(composite, SWT.NONE);
		GridData gd_btnCancel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnCancel.widthHint = 100;
		btnCancel.setLayoutData(gd_btnCancel);
		btnCancel.setImage(new Image(null, ExportWebArchiveComposite.class.getResourceAsStream("/ide/export/war/cancel.png")));
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				getShell().close();
			}
		});
		btnCancel.setText("Cancel");
		
		btnBuild = new Button(composite, SWT.NONE);
		btnBuild.setImage(new Image(null, ExportWebArchiveComposite.class.getResourceAsStream("/ide/export/war/package.png")));
		GridData gd_btnBuild = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 3, 1);
		gd_btnBuild.widthHint = 100;
		btnBuild.setLayoutData(gd_btnBuild);
		btnBuild.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				
				if(txtFileName.getText().trim().length() < 1 || txtTargetDir.getText().trim().length() < 1) {
					MessageBox message = new MessageBox(getShell(), SWT.ICON_WARNING);
					message.setText("Invalid Input");
					message.setMessage("'Archive Name' and/or 'Output Directory' are invalid." + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Please review before build.");
					message.open();
				}else {
					if(completed == false) {
						try {
							log.setText("Initializing build..");
							JSONObject tags = new JSONObject();
							tags.put("DEPENDENCIES", getDependecies(project_information));
							tags.put("FILE_NAME", txtFileName.getText().trim());
							tags.put("TARGET_DIR", txtTargetDir.getText().trim());
							tags.put("BIN_DIR", getBuildDir(project_information));
							build_file_data = JSONStringUtility.updateHashTags(build_file_data.replaceAll("<ESC>",  "").replaceAll("</ESC>", ""), tags);
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Preparing build project...");
							Project project = new Project();
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Creating build file...");
							Environment.FileSystem.delete(build_file_path);
							Environment.FileSystem.touch(build_file_path, build_file_data);
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Loading build file...");
							project.setUserProperty("ant.file", build_file_path);
							project.init();
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Creating build helper...");
							ProjectHelper helper = ProjectHelper.getProjectHelper();
							project.addReference("ant.projectHelper", helper);
							helper.parse(project, new File(build_file_path));
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Building web archive...");
							project.executeTarget(project.getDefaultTarget());
							if(txtKeepBuildFile.getSelection() == false) {
								Environment.FileSystem.delete(build_file_path);
							}else {
								Environment.FileSystem.copy(build_file_path, tags.getString("TARGET_DIR") + Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR) + "build.xml");
							}
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Cleaning up...");
							Environment.FileSystem.delete(build_file_path);
							Environment.FileSystem.delete(project_information.getString("project_full_path") + Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR) + txtFileName.getText() + ".war");
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Build Completed.");
							
							completed = true;
							btnBuild.setText("Close");
							btnBuild.setImage(Environment.Resources.getImageFromResource("/ide/export/war/exit.png"));
							
							btnCancel.setVisible(false);
							
						}catch (Exception exception) {
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR) + exception.toString());
						}
					}else {
						getShell().getParent().getShell().close();
					}
				}
			}
		});
		btnBuild.setText("Build");
	}
	
	private static String getDependecies(JSONObject project_information) {
		String output = "";
		for(JSONObject classpath_entry : project_information.getJSONObject("classpath").getJSONArray("classpathentry").toArray(JSONObject.class)) {
			if(classpath_entry.has("kind")) {
				if(classpath_entry.getString("kind").equalsIgnoreCase("lib")) {
					output = output + "<copy file=\"" + classpath_entry.getString("path") + "\" todir=\"WebContent/WEB-INF/lib\"/>" + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR);
				}
			}
		}
		return output;
	}
	
	private static String getBuildDir(JSONObject project_information) {
		String output = "";
		for(JSONObject classpath_entry : project_information.getJSONObject("classpath").getJSONArray("classpathentry").toArray(JSONObject.class)) {
			if(classpath_entry.has("kind")) {
				if(classpath_entry.getString("kind").equalsIgnoreCase("output")) {
					output = classpath_entry.getString("path");
				}
			}
		}
		return output;
	}

}
