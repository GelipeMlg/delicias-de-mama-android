import { importPKCS8, SignJWT } from "npm:jose@5.9.6";

// Invocación exclusivamente desde el programador con secreto, nunca desde Android.
const required = (name: string) => { const value = Deno.env.get(name); if (!value) throw new Error(`Missing ${name}`); return value; };
async function equal(a: string, b: string) {
  const digest = (s: string) => crypto.subtle.digest("SHA-256", new TextEncoder().encode(s));
  const [x,y] = await Promise.all([digest(a),digest(b)]);
  return new Uint8Array(x).every((v,i) => v === new Uint8Array(y)[i]);
}
Deno.serve(async (request) => {
  if (request.method !== "POST") return new Response("Method not allowed", {status:405});
  if (!await equal(request.headers.get("x-push-secret") ?? "", required("PUSH_CRON_SECRET"))) return new Response("Unauthorized",{status:401});
  try {
    const account = JSON.parse(required("FCM_SERVICE_ACCOUNT"));
    const key = await importPKCS8(account.private_key,"RS256");
    const assertion = await new SignJWT({scope:"https://www.googleapis.com/auth/firebase.messaging"})
      .setProtectedHeader({alg:"RS256"}).setIssuer(account.client_email).setAudience("https://oauth2.googleapis.com/token")
      .setIssuedAt().setExpirationTime("1h").sign(key);
    const oauth = await fetch("https://oauth2.googleapis.com/token",{method:"POST",body:new URLSearchParams({grant_type:"urn:ietf:params:oauth:grant-type:jwt-bearer",assertion})});
    if (!oauth.ok) throw new Error("FCM authentication failed");
    const {access_token} = await oauth.json();
    const url = required("SUPABASE_URL") + "/rest/v1/";
    const service = required("SUPABASE_SERVICE_ROLE_KEY");
    const headers = {apikey:service,Authorization:`Bearer ${service}`,"Content-Type":"application/json"};
    const claimed = await fetch(url+"rpc/reclamar_push_app",{method:"POST",headers,body:"{}"});
    if(!claimed.ok) throw new Error("Queue unavailable");
    const jobs = await claimed.json();
    let sent=0, failed=0;
    for(const job of jobs) {
      // Solo datos: Android comprueba el destinatario y elimina duplicados incluso en segundo plano.
      const response = await fetch(`https://fcm.googleapis.com/v1/projects/${account.project_id}/messages:send`,{
        method:"POST",headers:{Authorization:`Bearer ${access_token}`,"Content-Type":"application/json"},
        body:JSON.stringify({message:{token:job.token,data:{eventId:job.eventId,userId:job.userId,title:job.title,body:job.body},android:{priority:"high",ttl:"86400s"}}})
      });
      if(response.ok) {
        const ack = await fetch(url+`envios_push_app?id=eq.${job.jobId}&lease=eq.${job.lease}`,{method:"PATCH",headers,body:JSON.stringify({enviado:true})});
        if(ack.ok) sent++; else failed++;
      } else {
        const error=await response.json().catch(()=>({}));
        if(error?.error?.details?.some((d: {errorCode?:string})=>d.errorCode==="UNREGISTERED")) {
          const removed=await fetch(url+`dispositivos_app?token=eq.${encodeURIComponent(job.token)}&usuario_id=eq.${job.userId}`,{method:"DELETE",headers});
          if(!removed.ok) failed++;
        } else failed++; // El lease permite reintentar después de dos minutos, hasta ocho intentos.
      }
    }
    return Response.json({sent,failed});
  } catch {
    // No escribir claves, tokens ni datos de clientes en los logs.
    return Response.json({error:"Notification dispatch failed"},{status:500});
  }
});
