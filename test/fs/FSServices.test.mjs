import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import { assertComponents, TestErrorBoundary } from "react-assert";
import FSServices from "../../fs/FSServices.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("FSServices.test.mjs", () => {
  it("should fail if no context when useServices", () => {
    //given
    // suppress intended error
    // see: https://github.com/facebook/react/issues/11098#issuecomment-412682721
    const savedConsoleError = console.error;
    const consoleErrorMock = mockFunction(() => {
      console.error = savedConsoleError;
    });
    console.error = consoleErrorMock;

    const Wrapper = () => {
      FSServices.useServices();
      return h(React.Fragment);
    };

    //when
    const result = TestRenderer.create(
      h(TestErrorBoundary, null, h(Wrapper))
    ).root;

    //then
    assert.deepEqual(consoleErrorMock.times, 1);
    assert.deepEqual(console.error, savedConsoleError);
    assertComponents(
      result.children,
      h(
        "div",
        null,
        "Error: FSServices.Context is not found." +
          "\nPlease, make sure you use FSServices.Context.Provider in parent components"
      )
    );
  });
});
