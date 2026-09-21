import psycopg2

try:
    conn = psycopg2.connect(
        host="***REMOVED***",
        database="neondb",
        user="***REMOVED***",
        password="***REMOVED***"
    )
    cur = conn.cursor()
    
    # 1. Get user ID
    cur.execute("SELECT id FROM journey_customers WHERE mobile = '7794971935'")
    user = cur.fetchone()
    
    if user:
        user_id = user[0]
        print(f"Found user ID: {user_id}")
        
        # 2. Update their pending reward to trigger NOW
        cur.execute("UPDATE customer_rewards SET activated_at = NOW() - INTERVAL '1 hour' WHERE customer_id = %s AND reward_status = 'PENDING_UNLOCK'", (user_id,))
        conn.commit()
        print(f"Updated {cur.rowcount} pending rewards to activate immediately.")
    else:
        print("User not found.")
        
    cur.close()
    conn.close()
except Exception as e:
    print(f"Error: {e}")
