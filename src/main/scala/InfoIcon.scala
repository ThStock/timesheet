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
  val black: Image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB)
  val white: BufferedImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB)
  val g2d = white.createGraphics()
  g2d.setColor(Color.red)
  g2d.fillRect(0, 0, 32, 32)
  g2d.dispose()
  if (SystemTray.isSupported) {
    val tray = SystemTray.getSystemTray()

    trayIcon = new TrayIcon(black, "Timesheet")
    trayIcon.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent): Unit = {
        System.exit(1)
      }
    })
    tray.add(trayIcon)
  }

  def signalWrite(): Unit = {
    trayIcon.setImage(white)

    new Thread() {
      override def run(): Unit = {
        Thread.sleep(1000)
        trayIcon.setImage(black)
      }
    }.start()
  }
}
