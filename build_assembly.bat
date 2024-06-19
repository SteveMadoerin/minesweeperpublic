@echo off
SETLOCAL EnableDelayedExpansion

REM Define the project paths
SET "PROJECTS[0]=C:\Playground\minesweeperpublic\ui"
SET "PROJECTS[1]=C:\Playground\minesweeperpublic\controller"
SET "PROJECTS[2]=C:\Playground\minesweeperpublic\model"
SET "PROJECTS[3]=C:\Playground\minesweeperpublic\persistence"

REM Loop through the project paths and run `sbt assembly` in a new cmd window
FOR %%i IN (0 1 2 3) DO (
    SET project=!PROJECTS[%%i]!
    ECHO Starting new cmd for sbt assembly in !project!
    start "Assembly - !project!" cmd /c "cd /d !project! && sbt assembly && pause"
)

ECHO All projects started building in separate cmd windows.
ENDLOCAL
