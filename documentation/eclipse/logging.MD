# Enabling console logging

* Add the following to your launch target VM arguments:

```
  -Dlog4j.configurationFile=log4j2.xml
```
  
* Create a log4j2.xml file in the projects /run directory containing the following:

```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration monitorInterval="60" status="info">
	<Appenders>

		<Console name="STDOUT" target="SYSTEM_OUT">
			<PatternLayout pattern="MC %-5p [%t %d] (%F:%L) %c{2}: %m%n" />
		</Console>

	</Appenders>

	<Loggers>

		<Root level="info">
			<AppenderRef ref="STDOUT" />
		</Root>

		<Logger level="debug" name="minecolonies">
			<AppenderRef ref="STDOUT" />
		</Logger>
		
	</Loggers>

</Configuration>
```
