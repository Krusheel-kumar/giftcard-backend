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

    mobile = "7794971935"
    
    # Delete from campaign_users just in case they used the old BOGO system
    try:
        cur.execute("DELETE FROM campaign_users WHERE mobile_number = %s", (mobile,))
    except Exception as e:
        conn.rollback()
        
    cur.execute("SELECT id FROM journey_customers WHERE mobile = %s", (mobile,))
    row = cur.fetchone()

    if row:
        customer_id = row[0]
        print(f"Found customer with ID: {customer_id}")
        
        cur.execute("DELETE FROM reward_redemptions WHERE customer_id = %s", (customer_id,))
        cur.execute("DELETE FROM customer_rewards WHERE customer_id = %s", (customer_id,))
        cur.execute("DELETE FROM journey_customers WHERE id = %s", (customer_id,))
        
        conn.commit()
        print("Successfully wiped user data!")
    else:
        print(f"User with mobile {mobile} not found.")

    cur.close()
    conn.close()
except Exception as e:
    print(f"Error: {e}")

