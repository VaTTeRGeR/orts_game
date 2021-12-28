java -server -Xms128m -Xmx1024m -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC -XX:+AlwaysPreTouch -XX:+UseNUMA -XX:-UseBiasedLocking -jar target/release/orts-0.0.1.jar
