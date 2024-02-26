type Command = Deno.Command;

export async function findRepositoryPath (): Promise<string> {
  const command: Command = new Deno.Command('git', { args: [ 'rev-parse', '--show-toplevel' ] });
  const { stdout } = await command.output();
  return new TextDecoder().decode(stdout).replaceAll(/\n$/g, '');
}

export async function addFilesWithGit (glob: string): Promise<void> {
  const command: Command = new Deno.Command('git', { args: [ 'add', glob ] });
  const { code, stderr } = await command.output();

  if (code != 0) {
    throw Error(`Command 'git add' failed with code ${code}.\nStderr: ${new TextDecoder().decode(stderr)}`);
  }
}

export async function readChangedFiles (): Promise<string[]> {
  const command: Command = new Deno.Command('git', { args: [ 'status', '--porcelain' ] });
  const { code, stdout, stderr } = await command.output();

  if (code != 0) {
    throw Error(`Command 'git status' failed with code ${code}.\nStderr: ${new TextDecoder().decode(stderr)}`);
  }

  return new TextDecoder()
    .decode(stdout)
    .replaceAll(/\n$/g, '')
    .split('\n')
    .map((line: string) => line.replaceAll(/^[ MRADU?]{2} /g, ''));
}
