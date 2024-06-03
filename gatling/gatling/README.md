Gatling test fro Minesweeper
===========================================

Usage:

sbt Gatling/test for all tests

or better option to run 1 specific test:

sbt 'GatlingIt/testOnly minesweeperpersistence.PersistenceBaseItSimulation'

sbt 'GatlingIt/testOnly minesweeperpersistence.PersistenceEnduranceItSimulation'
sbt 'GatlingIt/testOnly minesweeperpersistence.PersistenceLoadItSimulation'
sbt 'GatlingIt/testOnly minesweeperpersistence.PersistenceStressItSimulation'
sbt 'GatlingIt/testOnly minesweeperpersistence.PersistenceSparkItSimulation'