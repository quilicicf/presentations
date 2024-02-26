export const DEFAULT: string = 'color: unset';
export const GREEN: string = 'color: green';
export const CYAN: string = 'color: cyan';

export type LoggerOptions = {
  isVerbose: boolean,
  indentation: string,
  baseLevel: number,
}

export type LogOptions = {
  styles: string[],
  alwaysLog?: boolean,
  relativeLevel?: number,
}

function buildIndentation (loggerOptions: LoggerOptions, relativeLevel: number) {
  return new Array(loggerOptions.baseLevel + relativeLevel)
    .fill(loggerOptions.indentation)
    .join('');
}

export function log (message: string, loggerOptions: LoggerOptions, logOptions: LogOptions): void {
  if (loggerOptions.isVerbose || logOptions.alwaysLog) {
    const indentation: string = buildIndentation(loggerOptions, logOptions.relativeLevel || 0);
    console.log(`${indentation}${message}`, ...logOptions.styles || []);
  }
}
