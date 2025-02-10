/**
 * @typedef {import("../../../file/popups/EncodingsPopup.mjs").EncodingsPopupProps} EncodingsPopupProps
 */
import React from "react";
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import TestRenderer from "react-test-renderer";
import { assertComponents, mockComponent } from "react-assert";
import ListPopup from "@farjs/ui/popup/ListPopup.mjs";
import Encoding from "../../../file/Encoding.mjs";
import EncodingsPopup from "../../../file/popups/EncodingsPopup.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

EncodingsPopup.listPopup = mockComponent(ListPopup);

const { listPopup } = EncodingsPopup;

describe("EncodingsPopup.test.mjs", () => {
  it("should call onApply with new encoding when onAction", () => {
    //given
    let onApplyArgs = /** @type {any[]} */ ([]);
    const onApply = mockFunction((...args) => (onApplyArgs = args));
    const onClose = mockFunction();
    const props = getEncodingsPopupProps({ onApply, onClose });
    const comp = TestRenderer.create(h(EncodingsPopup, props)).root;
    const popupProps = comp.findByType(listPopup).props;

    //when
    popupProps.onAction(1);

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(onApply.times, 1);
    assert.deepEqual(onApplyArgs, ["big5"]);
  });

  it("should not call onApply if same encoding when onAction", () => {
    //given
    const onApply = mockFunction();
    const onClose = mockFunction();
    const props = getEncodingsPopupProps({
      encoding: "big5",
      onApply,
      onClose,
    });
    const comp = TestRenderer.create(h(EncodingsPopup, props)).root;
    const popupProps = comp.findByType(listPopup).props;

    //when
    popupProps.onAction(1);

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(onApply.times, 0);
  });

  it("should render popup", () => {
    //given
    const props = getEncodingsPopupProps({ encoding: "test_unknown" });

    //when
    const result = TestRenderer.create(h(EncodingsPopup, props)).root;

    //then
    assertEncodingsPopup(result, 0);
  });
});

/**
 * @param {Partial<EncodingsPopupProps>} props
 * @returns {EncodingsPopupProps}
 */
function getEncodingsPopupProps(props = {}) {
  return {
    encoding: "utf8",
    onApply: mockFunction(),
    onClose: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {number} selected
 */
function assertEncodingsPopup(result, selected) {
  assert.deepEqual(EncodingsPopup.displayName, "EncodingsPopup");

  assertComponents(
    result.children,
    h(listPopup, {
      title: "Encodings",
      items: Encoding.encodings,
      selected: selected,
      onAction: mockFunction(),
      onClose: mockFunction(),
    })
  );
}
