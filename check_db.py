import psycopg2

try:
    conn = psycopg2.connect(
        dbname="neondb",
        user="neondb_owner",
        password="npg_QRhy4IT5ZfJr",
        host="ep-super-lake-b40lv2hz-pooler.c-6.us-east-2.aws.neon.tech",
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

