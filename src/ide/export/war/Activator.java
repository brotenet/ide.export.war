package ide.export.war;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.util.converters.XML;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ide.export.war"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static String getSelectionPackage(Object selection) {
		if (selection == null) {
			selection = getSelection();
		}
		if (selection instanceof PackageFragment) {
			String package_name = "";
			for (String package_node : ((PackageFragment) selection).names) {
				package_name += "." + package_node;
			}
			package_name = package_name.substring(1);
			return package_name;
		} else if (selection instanceof PackageFragmentRoot) {
			return "";
		} else {
			return getSelectionPackage(((IStructuredSelection) ((IStructuredSelection) selection).getFirstElement()).getFirstElement());
		}
	}

	public static String getProjectPath() {
		Object item = getSelection();
		if(item instanceof PackageFragmentRoot) {
			return ((PackageFragmentRoot) item).getJavaProject().getProject().getLocation().toString();
		}else if(item instanceof PackageFragment) {
			return ((PackageFragment) item).getJavaProject().getProject().getLocation().toString();
		}else if(item instanceof Project) {
			return ((Project) item).getLocation().toString();
		}else if(item instanceof File) {
			return ((File) item).getProject().getLocation().toString();
		}else if(item instanceof Folder) {
			return ((Folder) item).getProject().getLocation().toString();
		}else {
			return ((IResource) item).getProject().getLocation().toString();
		}
	}

	public static IProject getProject() {
		Object item = getSelection();
		if(item instanceof PackageFragmentRoot) {
			return ((PackageFragmentRoot) item).getJavaProject().getProject();
		}else if(item instanceof PackageFragment) {
			return ((PackageFragment) item).getJavaProject().getProject();
		}else if(item instanceof Project) {
			return ((Project) item);
		}else if(item instanceof File) {
			return ((File) item).getProject();
		}else if(item instanceof Folder) {
			return ((Folder) item).getProject();
		}else {
			return ((IResource) item).getProject();
		}
	}
	
	public static Object getSelection() {
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		IStructuredSelection selection = (IStructuredSelection) selectionService.getSelection();
		return selection.getFirstElement();
	}
	
	public static String getSelectionPath() {
		Object item = getSelection();
		if(item instanceof PackageFragmentRoot) {
			return ((PackageFragmentRoot) item).getJavaProject().getProject().getLocation().toString();
		}else if(item instanceof PackageFragment) {
			return ((PackageFragment) item).getJavaProject().getProject().getLocation().toString();
		}else if(item instanceof Project) {
			return ((Project) item).getLocation().toString();
		}else if(item instanceof File) {
			return ((File) item).getProject().getLocation().toString();
		}else if(item instanceof Folder) {
			return ((Folder) item).getProject().getLocation().toString();
		}else {
			return ((IResource) item).getProject().getLocation().toString();
		}
	}
	
	public static String getSelectionPath(Object item) {
		if(item instanceof PackageFragmentRoot) {
			return ((PackageFragmentRoot) item).getJavaProject().getProject().getLocation().toString();
		}else if(item instanceof PackageFragment) {
			return ((PackageFragment) item).getJavaProject().getProject().getLocation().toString();
		}else if(item instanceof Project) {
			return ((Project) item).getLocation().toString();
		}else if(item instanceof File) {
			return ((File) item).getProject().getLocation().toString();
		}else if(item instanceof Folder) {
			return ((Folder) item).getProject().getLocation().toString();
		}else {
			return ((IResource) item).getProject().getLocation().toString();
		}
	}
	
	public static JSONObject getProjectInformation() {
		JSONObject output = new JSONObject();
		try {
			String project_full_path = getProjectPath();
			output.put("properties", XML.toJSONObject(new String(Files.readAllBytes(Paths.get(project_full_path + System.getProperty("file.separator") + ".project")))));
			output.put("classpath", XML.toJSONObject(new String(Files.readAllBytes(Paths.get(project_full_path + System.getProperty("file.separator") + ".classpath")))).getJSONObject("classpath"));
			output.put("project_full_path", project_full_path);
			output.put("libraries", new JSONArray());
			for (Object entry_object : output.getJSONObject("classpath").getJSONArray("classpathentry").toArray()) {
				JSONObject entry = (JSONObject) entry_object;
				if(entry.getString("kind").equalsIgnoreCase("src")) {
					output.put("source_full_path", project_full_path + System.getProperty("file.separator") + entry.getString("path"));
				}else if(entry.getString("kind").equalsIgnoreCase("output")) {
					output.put("output_full_path", project_full_path + System.getProperty("file.separator") + entry.getString("path"));
				}else if(entry.getString("kind").equalsIgnoreCase("lib")) {
					output.getJSONArray("libraries").put(project_full_path + System.getProperty("file.separator") + entry.getString("path"));
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return output;
	}
	
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	public static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	public static void setProjectFile(String file_name, String container_path, InputStream contents, IWizardContainer wizard_container, ISelection selection) throws Exception {
		@SuppressWarnings("unused")
		IRunnableWithProgress progress = new IRunnableWithProgress() {
			
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask("setProjectFile [" + file_name + "] ", 2);
					String selection_path = getSelectionPath(selection);
					
					IResource resource = getWorkspaceRoot().findMember(selection_path);
					if (!resource.exists() || !(resource instanceof IContainer)) {
						throw new Exception("Container \"" + container_path + "\" does not exist.")	;
					}else {
						IContainer container = (IContainer) resource;
						final IFile file = container.getFile(new Path(file_name));
						if(file.exists()) {
							file.setContents(contents, true,  true, monitor);
						}else {
							file.create(contents, true, monitor);
						}
						contents.close();
					}
					
				} catch (Exception exception) {
					try {
						throw new Exception(exception.getMessage());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				monitor.worked(1);
			}
		};
		wizard_container.run(true, false, progress);
	}
	
	public static void setProjectFile(String file_name, String container_path, String contents, IWizardContainer wizard_container, ISelection selection) throws Exception{
		setProjectFile(file_name, container_path, new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)), wizard_container, selection);
	}

}
