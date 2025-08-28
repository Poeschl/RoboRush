import log from "loglevel";
import prefix from "loglevel-plugin-prefix";
import type { ChalkInstance } from "chalk";
import chalk from "chalk";

export const useLogging = () => {
  const DEFAULT_LOCAL_LOGLEVEL = "debug";

  const LEVEL_COLORS: { [key: string]: ChalkInstance } = {
    TRACE: chalk.magenta,
    DEBUG: chalk.cyan,
    INFO: chalk.blue,
    WARN: chalk.yellow,
    ERROR: chalk.red,
  };

  const setup = () => {
    log.setLevel(DEFAULT_LOCAL_LOGLEVEL);

    prefix.reg(log);
    prefix.apply(log, {
      format(level, name, timestamp) {
        return `${chalk.gray(`[${timestamp}]`)} ${LEVEL_COLORS[level]!(level)}`;
      },
      levelFormatter(level) {
        return level.toUpperCase();
      },
      timestampFormatter(date) {
        return date.toISOString();
      },
    });
  };

  return { setup };
};
