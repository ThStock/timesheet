package timesheet

import java.awt.TrayIcon.MessageType
import java.awt.event.{MouseAdapter, MouseEvent, MouseListener}
import java.awt.image.BufferedImage
import java.awt.{Color, Image, SystemTray, Toolkit, TrayIcon}

object InfoIcon {
  def create(): InfoIcon = {
    new InfoIcon()
  }
}

class InfoIcon {
  var trayIcon: TrayIcon = null

  def trayImage(color: Color): BufferedImage = {
    val baseImagew: BufferedImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB)
    val g2d = baseImagew.createGraphics()
    g2d.setColor(color)
    g2d.fillRect(0, 0, 32, 32)
    g2d.dispose()
    baseImagew
  }

  val black: Image = trayImage(Color.black)
  val red: Image = trayImage(Color.red)
  val green: Image = trayImage(Color.green)
  var currentImage = red

  if (SystemTray.isSupported) {
    val tray = SystemTray.getSystemTray()

    trayIcon = new TrayIcon(black, "Timesheet")
    trayIcon.setImageAutoSize(true)
    trayIcon.addMouseListener(new MouseAdapter {

      val TIME_THRESHOLD = 300
      var clickCount = 0
      var lastClickTime = 0L

      override def mouseClicked(e: MouseEvent): Unit = {

        val currentTime = System.currentTimeMillis

        if (currentTime - lastClickTime > TIME_THRESHOLD) {
          clickCount = 1
          if (currentImage == red) {
            currentImage = green
          } else if (currentImage == green) {
            currentImage = red
          }
        } else {
          clickCount = clickCount + 1
          if (clickCount >= 5) {
            System.exit(0)
          }
        }
        lastClickTime = currentTime
      }

    })
    tray.add(trayIcon)
  }

  def signalWrite(): Unit = {
    trayIcon.setImage(currentImage)

    new Thread() {
      override def run(): Unit = {
        Thread.sleep(1000)
        trayIcon.setImage(black)
      }
    }.start()
  }
}
