/*! ******************************************************************************
 *
 * Hop : The Hop Orchestration Platform
 *
 * http://www.project-hop.org
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.ui.hopgui.file.workflow.delegates;

import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.Props;
import org.apache.hop.core.gui.plugin.GuiElementType;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.gui.plugin.GuiToolbarElement;
import org.apache.hop.core.logging.HopLogStore;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.action.ActionCopy;
import org.apache.hop.ui.core.dialog.EnterSelectionDialog;
import org.apache.hop.ui.core.gui.GuiCompositeWidgets;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.ui.hopgui.file.workflow.HopGuiWorkflowGraph;
import org.apache.hop.ui.hopgui.file.pipeline.HopGuiLogBrowser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.util.ArrayList;

@GuiPlugin
public class HopGuiWorkflowLogDelegate {
  private static Class<?> PKG = HopGuiWorkflowGraph.class; // for i18n purposes, needed by Translator!!

  private static final String GUI_PLUGIN_TOOLBAR_PARENT_ID = "HopGuiWorkflowLogDelegate-ToolBar";
  public static final String TOOLBAR_ICON_CLEAR_LOG_VIEW = "ToolbarIcon-10000-ClearLog";
  public static final String TOOLBAR_ICON_SHOW_ERROR_LINES = "ToolbarIcon-10010-ShowErrorLines";
  public static final String TOOLBAR_ICON_LOG_SETTINGS = "ToolbarIcon-10020-LogSettings";
  public static final String TOOLBAR_ICON_LOG_PAUSE_RESUME = "ToolbarIcon-10030-LogPauseResume";


  private HopGui hopGui;
  private HopGuiWorkflowGraph jobGraph;

  private CTabItem jobLogTab;

  public StyledText jobLogText;

  /**
   * The number of lines in the log tab
   */
  // private int textSize;
  private Composite jobLogComposite;

  private ToolBar toolbar;
  private GuiCompositeWidgets toolBarWidgets;

  private HopGuiLogBrowser logBrowser;

  /**
   * @param hopGui
   */
  public HopGuiWorkflowLogDelegate( HopGui hopGui, HopGuiWorkflowGraph jobGraph ) {
    this.hopGui = hopGui;
    this.jobGraph = jobGraph;
  }

  public void addJobLog() {
    // First, see if we need to add the extra view...
    //
    if ( jobGraph.extraViewComposite == null || jobGraph.extraViewComposite.isDisposed() ) {
      jobGraph.addExtraView();
    } else {
      if ( jobLogTab != null && !jobLogTab.isDisposed() ) {
        // just set this one active and get out...
        //
        jobGraph.extraViewTabFolder.setSelection( jobLogTab );
        return;
      }
    }

    // Add a pipelineLogTab : display the logging...
    //
    jobLogTab = new CTabItem( jobGraph.extraViewTabFolder, SWT.NONE );
    jobLogTab.setImage( GuiResource.getInstance().getImageShowLog() );
    jobLogTab.setText( BaseMessages.getString( PKG, "WorkflowGraph.LogTab.Name" ) );

    jobLogComposite = new Composite( jobGraph.extraViewTabFolder, SWT.NONE );
    jobLogComposite.setLayout( new FormLayout() );

    addToolBar();

    FormData fd = new FormData();
    fd.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fd.top = new FormAttachment( 0, 0 );
    fd.right = new FormAttachment( 100, 0 );
    toolbar.setLayoutData( fd );

    jobLogText = new StyledText( jobLogComposite, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    hopGui.getProps().setLook( jobLogText );
    FormData fdText = new FormData();
    fdText.left = new FormAttachment( 0, 0 );
    fdText.right = new FormAttachment( 100, 0 );
    fdText.top = new FormAttachment( (Control) toolbar, 0 );
    fdText.bottom = new FormAttachment( 100, 0 );
    jobLogText.setLayoutData( fdText );

    logBrowser = new HopGuiLogBrowser( jobLogText, jobGraph );
    logBrowser.installLogSniffer();

    // If the workflow is closed, we should dispose of all the logging information in the buffer and registry for it
    //
    jobGraph.addDisposeListener( event -> {
      if ( jobGraph.getWorkflow() != null ) {
        HopLogStore.discardLines( jobGraph.getWorkflow().getLogChannelId(), true );
      }
    } );

    jobLogTab.setControl( jobLogComposite );

    jobGraph.extraViewTabFolder.setSelection( jobLogTab );
  }

  private void addToolBar() {
    toolbar = new ToolBar( jobLogComposite, SWT.BORDER | SWT.WRAP | SWT.SHADOW_OUT | SWT.LEFT | SWT.HORIZONTAL );
    FormData fdToolBar = new FormData();
    fdToolBar.left = new FormAttachment( 0, 0 );
    fdToolBar.top = new FormAttachment( 0, 0 );
    fdToolBar.right = new FormAttachment( 100, 0 );
    toolbar.setLayoutData( fdToolBar );
    hopGui.getProps().setLook( toolbar, Props.WIDGET_STYLE_TOOLBAR );

    toolBarWidgets = new GuiCompositeWidgets( hopGui.getVariables() );
    toolBarWidgets.createCompositeWidgets( this, null, toolbar, GUI_PLUGIN_TOOLBAR_PARENT_ID, null );
    toolbar.pack();
  }

  public void clearLog() {
    if ( jobLogText != null && !jobLogText.isDisposed() ) {
      jobLogText.setText( "" );
    }
  }

  @GuiToolbarElement(
    id = TOOLBAR_ICON_LOG_SETTINGS,
    parentId = GUI_PLUGIN_TOOLBAR_PARENT_ID,
    type = GuiElementType.TOOLBAR_BUTTON,
    label = "WorkflowLog.Button.LogSettings",
    toolTip = "WorkflowLog.Button.LogSettings",
    i18nPackageClass = HopGui.class,
    image = "ui/images/log-settings.svg"
  )
  public void showLogSettings() {
    // TODO: implement or rethink
  }

  @GuiToolbarElement(
    id = TOOLBAR_ICON_SHOW_ERROR_LINES,
    parentId = GUI_PLUGIN_TOOLBAR_PARENT_ID,
    type = GuiElementType.TOOLBAR_BUTTON,
    label = "WorkflowLog.Button.ShowErrorLines",
    toolTip = "WorkflowLog.Button.ShowErrorLines",
    i18nPackageClass = HopGui.class,
    image = "ui/images/show-error-lines.svg"
  )
  public void showErrors() {
    String all = jobLogText.getText();
    ArrayList<String> err = new ArrayList<>();

    int i = 0;
    int startpos = 0;
    int crlen = Const.CR.length();

    String line = null;
    String lineUpper = null;
    while ( i < all.length() - crlen ) {
      if ( all.substring( i, i + crlen ).equalsIgnoreCase( Const.CR ) ) {
        line = all.substring( startpos, i );
        lineUpper = line.toUpperCase();
        if ( lineUpper.indexOf( BaseMessages.getString( PKG, "WorkflowLog.System.ERROR" ) ) >= 0
          || lineUpper.indexOf( BaseMessages.getString( PKG, "WorkflowLog.System.EXCEPTION" ) ) >= 0 ) {
          err.add( line );
        }
        // New start of line
        startpos = i + crlen;
      }

      i++;
    }
    line = all.substring( startpos );
    lineUpper = line.toUpperCase();
    if ( lineUpper.indexOf( BaseMessages.getString( PKG, "WorkflowLog.System.ERROR" ) ) >= 0
      || lineUpper.indexOf( BaseMessages.getString( PKG, "WorkflowLog.System.EXCEPTION" ) ) >= 0 ) {
      err.add( line );
    }

    if ( err.size() > 0 ) {
      String[] err_lines = new String[ err.size() ];
      for ( i = 0; i < err_lines.length; i++ ) {
        err_lines[ i ] = err.get( i );
      }

      EnterSelectionDialog esd = new EnterSelectionDialog( jobGraph.getShell(), err_lines,
        BaseMessages.getString( PKG, "WorkflowLog.Dialog.ErrorLines.Title" ),
        BaseMessages.getString( PKG, "WorkflowLog.Dialog.ErrorLines.Message" ) );
      line = esd.open();
      if ( line != null ) {
        WorkflowMeta workflowMeta = jobGraph.getManagedObject();
        for ( i = 0; i < workflowMeta.nrActions(); i++ ) {
          ActionCopy entryCopy = workflowMeta.getAction( i );
          if ( line.indexOf( entryCopy.getName() ) >= 0 ) {
            jobGraph.editJobEntry( workflowMeta, entryCopy );
          }
        }
      }
    }
  }

  /**
   * @return the workflow log tab
   */
  public CTabItem getJobLogTab() {
    return jobLogTab;
  }

  @GuiToolbarElement(
    id = TOOLBAR_ICON_LOG_PAUSE_RESUME,
    type = GuiElementType.TOOLBAR_BUTTON,
    label = "WorkflowLog.Button.Pause",
    toolTip = "WorkflowLog.Button.Pause",
    i18nPackageClass = HopGui.class,
    image = "ui/images/pause-log.svg",
    parentId = GUI_PLUGIN_TOOLBAR_PARENT_ID,
    separator = true
  )
  public void pauseLog() {
    ToolItem item = toolBarWidgets.findToolItem( TOOLBAR_ICON_LOG_PAUSE_RESUME );
    if ( logBrowser.isPaused() ) {
      logBrowser.setPaused( false );
      item.setImage( GuiResource.getInstance().getImageContinueLog() );
    } else {
      logBrowser.setPaused( true );
      item.setImage( GuiResource.getInstance().getImagePauseLog() );
    }
  }

  public boolean hasSelectedText() {
    return jobLogText != null && !jobLogText.isDisposed() && StringUtils.isNotEmpty( jobLogText.getSelectionText() );
  }

  public void copySelected() {
    if ( hasSelectedText() ) {
      jobGraph.jobClipboardDelegate.toClipboard( jobLogText.getSelectionText() );
    }
  }
}