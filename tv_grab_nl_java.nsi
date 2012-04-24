; Name of our application
Name "tv_grab_nl_java"

; The file to write
OutFile "../Setup-tv_grab_nl_java-${VERSION}.exe"

; Set the default Installation Directory
InstallDir "$PROGRAMFILES\tv_grab_nl_java"

; Set the text which prompts the user to enter the installation directory
DirText "Please choose a directory to which you'd like to install this application."

; ----------------------------------------------------------------------------------
; *************************** SECTION FOR INSTALLING *******************************
; ----------------------------------------------------------------------------------

Section "" ; A "useful" name is not needed as we are not installing separate components

; Set output path to the installation directory. Also sets the working
; directory for shortcuts
SetOutPath $INSTDIR\

File /oname=tv_grab_nl_java.jar target/tv_grab_nl_java-${VERSION}-dep.jar
File /oname=readme.txt README
File /oname=changelog.txt Changelog
File /oname=license.txt LICENSE

WriteUninstaller $INSTDIR\Uninstall.exe

; ///////////////// CREATE SHORT CUTS //////////////////////////////////////

CreateDirectory "$SMPROGRAMS\tv_grab_nl_java"


CreateShortCut "$SMPROGRAMS\tv_grab_nl_java\Configure tv_grab_nl_java.lnk" \
  "$SYSDIR\java.exe" \
  '-jar "$INSTDIR\tv_grab_nl_java.jar" --configure'
CreateShortCut "$SMPROGRAMS\tv_grab_nl_java\Run tv_grab_nl_java.lnk" \
  "$SYSDIR\java.exe" \
  '-jar "$INSTDIR\tv_grab_nl_java.jar" --output tv.xml'
CreateShortCut "$SMPROGRAMS\tv_grab_nl_java\README.lnk" "$INSTDIR\readme.txt"
CreateShortCut "$SMPROGRAMS\tv_grab_nl_java\Changelog.lnk" "$INSTDIR\changelog.txt"
CreateShortCut "$SMPROGRAMS\tv_grab_nl_java\LICENSE.lnk" "$INSTDIR\license.txt"


CreateShortCut "$SMPROGRAMS\tv_grab_nl_java\Uninstall tv_grab_nl_java.lnk" "$INSTDIR\Uninstall.exe"

; ///////////////// END CREATING SHORTCUTS //////////////////////////////////

; //////// CREATE REGISTRY KEYS FOR ADD/REMOVE PROGRAMS IN CONTROL PANEL /////////

WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\tv_grab_nl_java" "DisplayName"\
"tv_grab_nl_java (remove only)"

WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\tv_grab_nl_java" "UninstallString" \
"$INSTDIR\Uninstall.exe"

; //////////////////////// END CREATING REGISTRY KEYS ////////////////////////////

MessageBox MB_OK "Installation was successful."

SectionEnd

; ----------------------------------------------------------------------------------
; ************************** SECTION FOR UNINSTALLING ******************************
; ----------------------------------------------------------------------------------

Section "Uninstall"
; remove all the files and folders
Delete $INSTDIR\Uninstall.exe ; delete self
Delete $INSTDIR\tv_grab_nl_java.jar
Delete $INSTDIR\readme.txt
Delete $INSTDIR\license.txt
Delete $INSTDIR\changelog.txt
;Delete $INSTDIR\createInstaller1.nsi

RMDir $INSTDIR

; now remove all the startmenu links
Delete "$SMPROGRAMS\tv_grab_nl_java\Configure tv_grab_nl_java.lnk"
Delete "$SMPROGRAMS\tv_grab_nl_java\Run tv_grab_nl_java.lnk"
Delete "$SMPROGRAMS\tv_grab_nl_java\Uninstall tv_grab_nl_java.lnk"
Delete "$SMPROGRAMS\tv_grab_nl_java\README.lnk"
Delete "$SMPROGRAMS\tv_grab_nl_java\Changelog.lnk"
Delete "$SMPROGRAMS\tv_grab_nl_java\LICENSE.lnk"
RMDIR "$SMPROGRAMS\tv_grab_nl_java"

; Now delete registry keys
DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\tv_grab_nl_java"
DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\tv_grab_nl_java"

SectionEnd

