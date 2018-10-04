package org.zella.maryana.ui

import javafx.application.{Application, Platform}
import javafx.fxml.FXMLLoader
import javafx.scene.{Node, Scene}
import javafx.scene.control._
import javafx.scene.layout.{Background, VBox}
import javafx.scene.text.{Text, TextAlignment, TextFlow}
import javafx.stage.Stage
import java.io.IOException

import akka.actor.ActorSystem
import javafx.collections.FXCollections
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.control.Alert.AlertType
import org.zella.maryana.core.Net.Mark
import org.zella.maryana.core.{Api, Net, ProcessRunner}
import org.zella.maryana.ui.MainApp.AppPackage

class MainApp extends Application with IView {

  private var uninstallButton: Button = _
  private var appDetailText: TextFlow = _
  private var adbAboutLabel: Label = _
  private var deviceLabel: Label = _
  private var appList: ListView[AppPackage] = _
  private var adbExecTextField: TextField = _
  private var refreshButton: Button = _

  private var api: Api = _

  implicit val system = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }

  import java.util.prefs.Preferences

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
      appList = scene.lookup("#listApps").asInstanceOf[ListView[AppPackage]]

      appList.setCellFactory(param => new ListCell[AppPackage]() {
        override protected def updateItem(item: AppPackage, empty: Boolean): Unit = {
          super.updateItem(item, empty)
          if (empty || item == null) setText(null)
          else setText(item.app)
        }
      })

      adbExecTextField =
        scene.lookup("#adbExecTextField").asInstanceOf[TextField]
      refreshButton = scene.lookup("#refreshButton").asInstanceOf[Button]

      refreshButton.setOnAction((event: ActionEvent) => refreshAll())
      uninstallButton.setOnAction((event: ActionEvent) => {
        val selected = appList.getSelectionModel.getSelectedItem.app
        val pck = selected.substring(selected.lastIndexOf("=")+1)
        val alert = new Alert(
          AlertType.CONFIRMATION,
          "Warning! Delete " +pck + " ? Removing system apps may brick your phone!",
          ButtonType.YES,
          ButtonType.NO)
        alert.showAndWait()
        if (alert.getResult() == ButtonType.YES) {
          api.removePackage(pck)
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
    system.terminate()
    super.stop()  //FIXME dont use
  }

  def refreshAll(): Unit = {
    api = new Api(adbExecTextField.getText, new ProcessRunner, new Net(), MainApp.this)
    appDetailText.getChildren.clear()
    appList.getItems.clear()
    api.adbAbout()
    api.checkConnectedDevices()
    api.listAllPackages()

  }

  override def showCommandError(text: String): Unit = {
    applyFailure(appDetailText)
    setAppDetail(new Text(text))
  }

  override def showInstalledPackages(packages: Seq[AppPackage]): Unit = {
    Platform.runLater(() => {
      appList.getItems.clear()
      appList.getItems.addAll(packages: _*)
      uninstallButton.setDisable(packages.isEmpty)
    })
  }

  override def showUninstallResult(text: String): Unit = {
    if (text.equalsIgnoreCase("Success"))
      applySuccess(appDetailText)
    else
      applyFailure(appDetailText)
    setAppDetail(new Text(text))
    api.listAllPackages()
  }

  override def showAdbVer(adbVer: String): Unit = {
    applySuccess(adbAboutLabel)
    adbAboutLabel.setText(adbVer)
  }

  override def showAdbVerFailed(adbVer: String): Unit = {
    applyFailure(adbAboutLabel)
    adbAboutLabel.setText(adbVer)
  }

  override def showDeviceConnected(): Unit = {
    applySuccess(deviceLabel)
    deviceLabel.setText("device connected")
  }

  override def showNoDeviceConnected(): Unit = {
    applyFailure(deviceLabel)
    deviceLabel.setText("device not connected")
  }

  private def setAppDetail(node: Node): Unit = {
    appDetailText.getChildren.clear()
    appDetailText.getChildren.add(node)
  }

  private def applySuccess(elem: Node): Unit =
    elem.setStyle("-fx-text-fill: green; -fx-font-size: 13px;")

  private def applyFailure(elem: Node): Unit =
    elem.setStyle("-fx-text-fill: red; -fx-font-size: 13px;")

}

object MainApp {
  val PREF_ADB = "pref_adb_path"

  case class AppPackage(app: String, mark: Option[Mark]) {
//    override def equals(obj: scala.Any): Boolean = app.equals(obj.asInstanceOf[AppPackage].app)
//
//    override def hashCode(): Int = app.hashCode
  }

}
