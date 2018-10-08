package org.zella.maryana.ui

import java.io.IOException
import java.util.concurrent.Executors

import akka.actor.ActorSystem
import javafx.application.{Application, Platform}
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXMLLoader
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout.VBox
import javafx.scene.text.{Text, TextFlow}
import javafx.scene.{Node, Scene}
import javafx.stage.Stage
import monix.execution.Scheduler
import org.zella.maryana.controller.Controller
import org.zella.maryana.core.ProcessRunner
import org.zella.maryana.core.adb.IAdb.InstalledApp
import org.zella.maryana.core.adb.impl.LocalAdb
import org.zella.maryana.core.net.impl.SttpNet
import monix.execution.Scheduler.Implicits.global
import java.util.prefs.Preferences

class MainApp extends Application with IView {

  private var uninstallButton: Button = _
  private var appDetailText: TextFlow = _
  private var adbAboutLabel: Label = _
  private var deviceLabel: Label = _
  private var appList: ListView[InstalledApp] = _
  private var adbExecTextField: TextField = _
  private var refreshButton: Button = _

  private val controller: Controller = new Controller(new LocalAdb(new ProcessRunner), new SttpNet, this)

  private val prefs = Preferences.userNodeForPackage(this.getClass)

  def init(primaryStage: Stage): Unit =
    try { // Загружаем корневой макет из fxml файла.

      val loader = new FXMLLoader
      loader.setLocation(this.getClass.getResource("/main.fxml"))
      val rootLayout = loader.load.asInstanceOf[VBox]
      val scene = new Scene(rootLayout)
      uninstallButton = scene.lookup("#buttonUninstall").asInstanceOf[Button]
      appDetailText = scene.lookup("#textDetail").asInstanceOf[TextFlow]
      appDetailText.getChildren.add(new Text("Here will be some info..."))

      adbAboutLabel = scene.lookup("#labelAdbAbout").asInstanceOf[Label]
      deviceLabel = scene.lookup("#labelDevice").asInstanceOf[Label]
      appList = scene.lookup("#listApps").asInstanceOf[ListView[InstalledApp]]

      appList.setCellFactory(param => new ListCell[InstalledApp]() {
        override protected def updateItem(item: InstalledApp, empty: Boolean): Unit = {
          super.updateItem(item, empty)
          if (empty || item == null) setText(null)
          else setText(item.pkg)
        }
      })

      adbExecTextField =
        scene.lookup("#adbExecTextField").asInstanceOf[TextField]
      refreshButton = scene.lookup("#refreshButton").asInstanceOf[Button]

      refreshButton.setOnAction((event: ActionEvent) => refreshAll())
      uninstallButton.setOnAction((event: ActionEvent) => {
        val selected = appList.getSelectionModel.getSelectedItem.pkg
        val alert = new Alert(
          AlertType.CONFIRMATION,
          "Warning! Delete " + selected + " ? Removing system apps may brick your phone!",
          ButtonType.YES,
          ButtonType.NO)
        alert.showAndWait()
        if (alert.getResult() == ButtonType.YES) {
          controller.removePackage(selected)
        }
      })
      adbExecTextField.setText("asddasadsasdasd")
      uninstallButton.setDisable(true)
      appList.setItems(FXCollections.observableArrayList())

      //      val flow = new TextFlow(new Hyperlink("www.google.com"))
      //      scene.setRoot(flow)
      //  onViewCreated()

      primaryStage.setScene(scene)
      primaryStage.show()

      onViewCreated()
    } catch {
      case e: IOException =>
    }

  private def onViewCreated(): Unit = {
    adbExecTextField.setText(prefs.get(MainApp.PREF_ADB, "c:\\changeMe(will be saved en exit)\\adb.exe"))
    refreshAll()
  }

  @throws[Exception]
  override def start(stage: Stage): Unit = {
    stage.setTitle("Adb app uninstaller")
    //        stage.getIcons().add(new Image(Mp3SpbDownloader.class.getResourceAsStream("/icon.jpg")));
    //TODO
    //        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
    //            java.awt.Image image = Toolkit.getDefaultToolkit().getImage(Mp3SpbDownloader.class.getResource("/icon.jpg"));
    //            com.apple.eawt.Application.getApplication().setDockIconImage(image);
    //        }
    init(stage)
  }

  override def stop(): Unit = {
    prefs.put(MainApp.PREF_ADB, adbExecTextField.getText.trim)
    super.stop()
  }

  def refreshAll(): Unit = {
    controller.initialize(adbExecTextField.getText)
    appDetailText.getChildren.clear()
    appList.getItems.clear()
    controller.adbAbout()
    controller.checkConnectedDevices()
    controller.listAllPackages()
  }

  override def showCommandError(text: String): Unit = {
    Platform.runLater(() => {
      applyFailure(appDetailText)
      setAppDetail(new Text(text))
    })
  }

  override def showUninstallResult(text: String): Unit = {
    Platform.runLater(() => {
      if (text.equalsIgnoreCase("Success"))
        applySuccess(appDetailText)
      else
        applyFailure(appDetailText)
      setAppDetail(new Text(text))
      controller.listAllPackages()
    })
  }

  override def showAdbVer(adbVer: String): Unit = {
    Platform.runLater(() => {
      applySuccess(adbAboutLabel)
      adbAboutLabel.setText(adbVer)
    })
  }

  override def showAdbVerFailed(adbVer: String): Unit = {
    Platform.runLater(() => {
      applyFailure(adbAboutLabel)
      adbAboutLabel.setText(adbVer)
    })
  }

  override def showDeviceConnected(): Unit = {
    Platform.runLater(() => {
      applySuccess(deviceLabel)
      deviceLabel.setText("device connected")
    })
  }

  override def showNoDeviceConnected(): Unit = {
    Platform.runLater(() => {
      applyFailure(deviceLabel)
      deviceLabel.setText("device not connected")
    })
  }

  private def setAppDetail(node: Node): Unit = {
    Platform.runLater(() => {
      appDetailText.getChildren.clear()
      appDetailText.getChildren.add(node)
    })
  }

  private def applySuccess(elem: Node): Unit =
    elem.setStyle("-fx-text-fill: green; -fx-font-size: 13px;")

  private def applyFailure(elem: Node): Unit =
    elem.setStyle("-fx-text-fill: red; -fx-font-size: 13px;")

  override def showProgress(): Unit = "TODO"

  override def statusBar(msg: String): Unit = "TODO"

  override def clearStatusBar(): Unit = "TODO"

  override def cancelProgress(): Unit = "TODO"

  override def showInstalledApps(apps: Seq[InstalledApp]): Unit = {
    Platform.runLater(() => {
      appList.getItems.clear()
      appList.getItems.addAll(apps: _*)
      uninstallButton.setDisable(apps.isEmpty)
    })
  }
}

object MainApp {
  val PREF_ADB = "pref_adb_path"
}
