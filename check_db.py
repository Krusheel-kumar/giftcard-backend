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

    cur.execute("SELECT COUNT(*) FROM journey_customers;")
    users = cur.fetchone()[0]
    
    cur.execute("SELECT COUNT(*) FROM customer_rewards;")
    rewards = cur.fetchone()[0]
    
    cur.execute("SELECT COUNT(*) FROM reward_redemptions;")
    redemptions = cur.fetchone()[0]

    print(f"Users found: {users}")
    print(f"Rewards found: {rewards}")
    print(f"Redemptions found: {redemptions}")

    cur.close()
    conn.close()
except Exception as e:
    print(f"Error: {e}")

