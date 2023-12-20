#!/usr/bin/env deno

async function main () {
  const [
    jenkinsURL,
    groovyScriptPath,
    jenkinsLoginVariable,
    jenkinsTokenVariable,
  ] = Deno.args;
  const jenkinsLogin = Deno.env.get(jenkinsLoginVariable);
  const jenkinsToken = Deno.env.get(jenkinsTokenVariable);
// curl --data-urlencode "script=$(cat /tmp/system-message-example.groovy)" -v --user username:ApiToken https://jenkins.example.com/scriptText

  const groovyScript = await Deno.readTextFile(groovyScriptPath);
  const response = await fetch(`${jenkinsURL}/scriptText`, {
    method: 'POST',
    headers: { Authorization: `Basic ${btoa(`${jenkinsLogin}:${jenkinsToken}`)}` },
    body: new URLSearchParams({ script: groovyScript }),
  });
}

main()
  .catch((error) => {
    console.error(`Failed with error: ${error.stack}\n`);
    Deno.exit(1);
  });
