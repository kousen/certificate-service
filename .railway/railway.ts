import { defineRailway, project, service } from "railway/iac";
// This repository manages only its own resources in the environment. Other
// repositories export their own partial name.
// See https://docs.railway.com/infrastructure-as-code#multi-repo-projects
export const partial = "certificate-service";
export default defineRailway(() => {
  const certificateService = service("certificate-service", {
    build: { buildEnvironment: "V3", builder: "RAILPACK" },
    start: "sh -c 'echo \"$CERTIFICATE_KEYSTORE_B64\" | base64 -d > /tmp/keystore.p12 && exec java -Dserver.port=${PORT:-8080} -jar /app/build/libs/app.jar'",
    healthcheck: "/actuator/health",
    replicas: { "us-east4-eqdc4a": 1 },
    domains: ["certificate-service.kousenit.com"],
  });

  return project("certificate-service", {
    variables: { managed: false },
    resources: [certificateService],
  });
});
