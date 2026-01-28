import { lazyFn } from "@farjs/filelist/utils.mjs";

/**
 * @typedef {{
 *  readonly year: number;
 *  readonly month: number;
 *  readonly date: number;
 *  readonly hours: number;
 *  readonly minutes: number;
 *  readonly seconds: number;
 * }} DateTimeData
 */

const DateTimeUtil = {
  /**
   * @param {string} input
   * @returns {number}
   */
  parseDateTime: (input) => {
    /** @type {() => DateTimeData | undefined} */
    const MMddyyyyTime = () => {
      const regexRes = MMddyyyyTimeRegex().exec(input);
      if (regexRes) {
        return {
          month: parseInt(regexRes[1]),
          date: parseInt(regexRes[2]),
          year: parseInt(regexRes[3]),
          hours: parseInt(regexRes[4]),
          minutes: parseInt(regexRes[5]),
          seconds: 0,
        };
      }
      return undefined;
    };

    /** @type {() => DateTimeData | undefined} */
    const ddMMyyTime = () => {
      const regexRes = ddMMyyTimeRegex().exec(input);
      if (regexRes) {
        return {
          date: parseInt(regexRes[1]),
          month: parseInt(regexRes[2]),
          year: parseInt(regexRes[3]),
          hours: parseInt(regexRes[4]),
          minutes: parseInt(regexRes[5]),
          seconds: 0,
        };
      }
      return undefined;
    };

    /** @type {() => DateTimeData | undefined} */
    const yyyyMMddHHmmss = () => {
      const regexRes = yyyyMMddHHmmssRegex().exec(input);
      if (regexRes) {
        return {
          year: parseInt(regexRes[1]),
          month: parseInt(regexRes[2]),
          date: parseInt(regexRes[3]),
          hours: parseInt(regexRes[4]),
          minutes: parseInt(regexRes[5]),
          seconds: parseInt(regexRes[6]),
        };
      }
      return undefined;
    };

    const data = MMddyyyyTime() || ddMMyyTime() || yyyyMMddHHmmss();
    if (data) {
      const fullYear = (() => {
        if (data.year < 100) {
          const after2000 = data.year + 2000;
          return after2000 > currYear ? data.year + 1900 : after2000;
        }
        return data.year;
      })();

      return new Date(
        fullYear,
        data.month - 1,
        data.date,
        data.hours,
        data.minutes,
        data.seconds,
      ).getTime();
    }
    return 0;
  },
};

const currYear = new Date().getFullYear();

/** @type {() => RegExp} */
const MMddyyyyTimeRegex = lazyFn(
  () => /(\d{2})-(\d{2})-(\d{4}) (\d{2}):(\d{2})/,
);

/** @type {() => RegExp} */
const ddMMyyTimeRegex = lazyFn(
  () => /(\d{2})\.(\d{2})\.(\d{2}) (\d{2}):(\d{2})/,
);

/** @type {() => RegExp} */
const yyyyMMddHHmmssRegex = lazyFn(
  () => /(\d{4})(\d{2})(\d{2})\.(\d{2})(\d{2})(\d{2})/,
);

export default DateTimeUtil;
