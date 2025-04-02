/**
 * @typedef {import("../../viewer/ViewerSearch.mjs").ViewerSearchProps} ViewerSearchProps
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import StatusPopup from "@farjs/ui/popup/StatusPopup.mjs";
import ViewerSearch from "../../viewer/ViewerSearch.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

ViewerSearch.statusPopupComp = mockComponent(StatusPopup);

const { statusPopupComp } = ViewerSearch;

describe("ViewerSearch.test.mjs", () => {
  it("should call onComplete when onClose", () => {
    //given
    const onComplete = mockFunction();
    const props = getViewerSearchProps({ onComplete });
    const comp = TestRenderer.create(h(ViewerSearch, props)).root;
    const statusPopupProps = comp.findByType(statusPopupComp).props;

    //when
    statusPopupProps.onClose();

    //then
    assert.deepEqual(onComplete.times, 1);
  });

  it("should render status popup", () => {
    //given
    const props = getViewerSearchProps();

    //when
    const result = TestRenderer.create(h(ViewerSearch, props)).root;

    //then
    assertViewerSearch(result, props);
  });
});

/**
 * @param {Partial<ViewerSearchProps>} props
 * @returns {ViewerSearchProps}
 */
function getViewerSearchProps(props = {}) {
  return {
    searchTerm: "test",
    onComplete: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {ViewerSearchProps} props
 */
function assertViewerSearch(result, props) {
  assert.deepEqual(ViewerSearch.displayName, "ViewerSearch");

  assertComponents(
    result.children,
    h(statusPopupComp, {
      text: `Searching for\n"${props.searchTerm}"`,
      title: "Search",
    })
  );
}
