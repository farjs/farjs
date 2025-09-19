/**
 * @import { DriveControllerProps } from "../../../fs/popups/DriveController.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import DrivePopup from "../../../fs/popups/DrivePopup.mjs";
import DriveController from "../../../fs/popups/DriveController.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

DriveController.drivePopup = mockComponent(DrivePopup);

const { drivePopup } = DriveController;

describe("DriveController.test.mjs", () => {
  it("should call onChangeDir and onClose when onChangeDir on the left", () => {
    //given
    const dispatch = mockFunction();
    let onChangeDirArgs = /** @type {any[]} */ ([]);
    const onChangeDir = mockFunction((...args) => (onChangeDirArgs = args));
    const onClose = mockFunction();
    const props = getDriveControllerProps({
      dispatch,
      showDrivePopupOnLeft: true,
      onChangeDir,
      onClose,
    });
    const comp = TestRenderer.create(h(DriveController, props)).root;
    const popupProps = comp.findByType(drivePopup).props;
    const dir = "test dir";

    //when
    popupProps.onChangeDir(dir);

    //then
    assert.deepEqual(onChangeDir.times, 1);
    assert.deepEqual(onChangeDirArgs, [dir, true]);
    assert.deepEqual(onClose.times, 1);
  });

  it("should call onChangeDir and onClose when onChangeDir on the right", () => {
    //given
    const dispatch = mockFunction();
    let onChangeDirArgs = /** @type {any[]} */ ([]);
    const onChangeDir = mockFunction((...args) => (onChangeDirArgs = args));
    const onClose = mockFunction();
    const props = getDriveControllerProps({
      dispatch,
      showDrivePopupOnLeft: false,
      onChangeDir,
      onClose,
    });
    const comp = TestRenderer.create(h(DriveController, props)).root;
    const popupProps = comp.findByType(drivePopup).props;
    const dir = "test dir";

    //when
    popupProps.onChangeDir(dir);

    //then
    assert.deepEqual(onChangeDir.times, 1);
    assert.deepEqual(onChangeDirArgs, [dir, false]);
    assert.deepEqual(onClose.times, 1);
  });

  it("should call onClose when onClose", () => {
    //given
    const dispatch = mockFunction();
    const onChangeDir = mockFunction();
    const onClose = mockFunction();
    const props = getDriveControllerProps({
      dispatch,
      showDrivePopupOnLeft: true,
      onChangeDir,
      onClose,
    });
    const comp = TestRenderer.create(h(DriveController, props)).root;
    const popupProps = comp.findByType(drivePopup).props;

    //when
    popupProps.onClose();

    //then
    assert.deepEqual(onChangeDir.times, 0);
    assert.deepEqual(onClose.times, 1);
  });

  it("should render popup on the left", () => {
    //given
    const dispatch = mockFunction();
    const props = getDriveControllerProps({
      dispatch,
      showDrivePopupOnLeft: true,
    });

    //when
    const renderer = TestRenderer.create(h(DriveController, props));

    //then
    assertDriveController(renderer.root, true);
  });

  it("should render popup on the right", () => {
    //given
    const dispatch = mockFunction();
    const props = getDriveControllerProps({
      dispatch,
      showDrivePopupOnLeft: false,
    });

    //when
    const renderer = TestRenderer.create(h(DriveController, props));

    //then
    assertDriveController(renderer.root, false);
  });

  it("should render empty component", () => {
    //given
    const dispatch = mockFunction();
    const props = getDriveControllerProps({
      dispatch,
      showDrivePopupOnLeft: undefined,
    });

    //when
    const renderer = TestRenderer.create(h(DriveController, props));

    //then
    assert.deepEqual(renderer.root.children.length, 0);
  });
});

/**
 * @param {Partial<DriveControllerProps>} props
 * @returns {DriveControllerProps}
 */
function getDriveControllerProps(props = {}) {
  return {
    dispatch: mockFunction(),
    showDrivePopupOnLeft: false,
    onChangeDir: mockFunction(),
    onClose: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {boolean} showOnLeft
 */
function assertDriveController(result, showOnLeft) {
  assert.deepEqual(DriveController.displayName, "DriveController");

  assertComponents(
    result.children,
    h(drivePopup, {
      dispatch: mockFunction(),
      onChangeDir: mockFunction(),
      onClose: mockFunction(),
      showOnLeft,
    })
  );
}
