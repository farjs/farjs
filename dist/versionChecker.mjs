export default {};

import https from "https";
import { readFile } from "fs/promises";

const packageJson = JSON.parse(
  await readFile(new URL("../package.json", import.meta.url), "utf8")
);

export function fetchLatestVersion() {
  return new Promise((resolve, reject) => {
    const httpOptions = {
      headers: {
        "accept-encoding": "utf8",
      },
    };
    https
      .get(
        `https://registry.npmjs.org/${packageJson.name}/latest`,
        httpOptions,
        (resp) => {
          try {
            const { statusCode } = resp;
            const contentType = /** @type {string} */ (
              resp.headers["content-type"]
            );

            let error;
            if (statusCode !== 200) {
              error = new Error(
                "Request Failed.\n" + `Status Code: ${statusCode}`
              );
            } else if (!/^application\/json/.test(contentType)) {
              error = new Error(
                "Invalid content-type.\n" +
                  `Expected application/json but received ${contentType}`
              );
            }
            if (error) {
              resp.resume(); // Consume response data to free up memory
              reject(error.message);
              return;
            }

            let rawData = "";
            resp.setEncoding("utf8");
            resp.on("data", (chunk) => {
              rawData += chunk;
            });
            resp.once("end", () => {
              try {
                const parsedData = JSON.parse(rawData);
                resolve(parsedData.version);
              } catch (e) {
                reject(e);
              }
            });
          } catch (e) {
            reject(e);
          }
        }
      )
      .once("error", (e) => {
        reject(e);
      });
  });
}

/**
 * @param {string} text
 * @returns {string}
 */
function green(text) {
  return "\u001b[32m" + text + "\u001b[39m";
}

/**
 * @param {string | undefined} npmVersion
 */
export function checkNpmVersion(npmVersion) {
  if (npmVersion && npmVersion != packageJson.version) {
    console.log(
      green(
        `
  There is a newer version of ${packageJson.name} available: ${npmVersion}
  (You are currently using version: ${packageJson.version})

  Install FAR.js globally using the package manager of your choice.
  For example, to get the latest version:

    npm i -g ${packageJson.name}

  See the changelog here: https://github.com/farjs/farjs/releases
`
      )
    );
  }
}
