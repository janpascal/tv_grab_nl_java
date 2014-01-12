#!/bin/sh

TVFILE=tv.xml
XSLT=xml2sql.xslt
SQLFILE=tv.sql
DB=test.sqlite

echo "Converting xmltv data to SQL..." >&2
saxonb-xslt -xsl:$XSLT  -s:$TVFILE -dtd:off >$SQLFILE
echo "Reading data into database..." >&2
rm $DB; sqlite3 $DB <$SQLFILE

echo "Fetching available dates..." >&2
DATES=$( sqlite3 $DB 'select distinct datum from programme order by datum asc' )

echo "<html>"
echo "<h2>Aantal kanalen per provider</h2>"

echo "Fetching number of channels per provider..." >&2
echo "<table>"
sqlite3 -html -header $DB 'select source as provider,count(*) as aantal from channel group by source;'
echo "</table>"

SQL='SELECT source'
for datum in $DATES
do
    SQL="$SQL , count(case when programme.datum=\"$datum\" then 1 end) as \`$datum\`"
done
SQL="$SQL from programme"
SQL="$SQL group by source"

echo "Fetching number of programmes per provider per day..." >&2
echo "<h2>Aantal programmas in de gids per provider per dag</h2>"
echo "<table>"
sqlite3 -html -header $DB "$SQL"
echo "</table>"

SQL='SELECT p.source, c.name'
for datum in $DATES
do
    SQL="$SQL , count(case when p.datum=\"$datum\" then 1 end) as \`$datum\`"
done
SQL="$SQL FROM programme p JOIN channel c ON c.id=p.channel"
SQL="$SQL group by c.name,c.source"
SQL="$SQL order by c.name"

echo "Fetching number of programmes per provider, per channel, per day..." >&2
echo "<h2>Aantal programmas in de gids per provider per kanaal per dag</h2>"
echo "<table>"
sqlite3 -html -header $DB "$SQL"
echo "</table>"

SQL='SELECT p.source, c.name'
for datum in $DATES
do
    SQL="$SQL , count(case when p.datum=\"$datum\" then 1 end) as \`$datum\`"
done
SQL="$SQL FROM programme p JOIN channel c ON c.id=p.channel"
SQL="$SQL group by c.name,c.source"
SQL="$SQL order by c.source"

echo "Fetching number of programmes per channel, per provider, per day..." >&2
echo "<h2>Aantal programmas in de gids per kanaal per provider per dag</h2>"
echo "<table>"
sqlite3 -html -header $DB "$SQL"
echo "</table>"

echo "</html>"
