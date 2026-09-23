import psycopg2

try:
    conn = psycopg2.connect(
        dbname="neondb",
        user="***REMOVED***",
        password="***REMOVED***",
        host="***REMOVED***",
        port="5432",
        sslmode="require"
    )
    cur = conn.cursor()

    cur.execute("SELECT id, name FROM reward_definitions")
    for row in cur.fetchall():
        print(row)
        
    cur.close()
    conn.close()
except Exception as e:
    print(e)

