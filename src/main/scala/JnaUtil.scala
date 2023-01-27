package timesheet

import com.sun.jna
import com.sun.jna.{Native, Pointer}
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.ptr.PointerByReference

import java.lang.annotation.Native
import java.time.ZonedDateTime

object JnaUtil {
  val MAX_TITLE_LENGTH: Int = 1024

  def getActiveWindowProcess(): String = {
    val buffer: Array[Char] = new Array(MAX_TITLE_LENGTH * 2)
    val pointer = new PointerByReference()
    val foregroundWindow = User32DLL.GetForegroundWindow()
    User32DLL.GetWindowThreadProcessId(foregroundWindow, pointer)
    val process = Kernel32.OpenProcess(Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ, false, pointer.getValue)
    Psapi.GetModuleBaseNameW(process, null, buffer, MAX_TITLE_LENGTH)
    jna.Native.toString(buffer)
  }

  def getActiveWindowTitle(): String = {
    val buffer: Array[Char] = new Array(MAX_TITLE_LENGTH * 2)
    val foregroundWindow = User32DLL.GetForegroundWindow()
    User32DLL.GetWindowTextW(foregroundWindow, buffer, MAX_TITLE_LENGTH);
    jna.Native.toString(buffer)
  }

  object Psapi {
    jna.Native.register("psapi")

    @native def GetModuleBaseNameW(hProcess: Pointer, hmodule: Pointer, lpBaseName: Array[Char], size: Int): Int
  }

  object Kernel32 {
    jna.Native.register("kernel32")
    val PROCESS_QUERY_INFORMATION: Int = 0x0400
    val PROCESS_VM_READ: Int = 0x0010

    @native def OpenProcess(dwDesiredAccess: Int, bInheritHandle: Boolean, pointer: Pointer): Pointer
  }

  object User32DLL {
    jna.Native.register("user32")

    @native def GetForegroundWindow(): HWND

    @native def GetWindowTextW(hWnd: HWND, lpString: Array[Char], nMaxCount: Int): Int

    @native def GetWindowThreadProcessId(hWnd: HWND, pref: PointerByReference): Int
  }
}
