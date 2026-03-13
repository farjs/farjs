/**
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").HistoryProvider} HistoryProvider
 * @typedef {import("../../../fs/FSServices.mjs").FSServices} FSServices
 * @import { WithStacksProps } from "@farjs/filelist/stack/WithStacksProps.mjs"
 * @import { FileListBrowserProps } from "../../../app/filelist/FileListBrowser.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import { deepEqual } from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import HistoryProvider from "@farjs/filelist/history/HistoryProvider.mjs";
import FSServices from "../../../fs/FSServices.mjs";
import testDb from "../../db.mjs";
import FileListRoot from "../../../app/filelist/FileListRoot.mjs";
import FileListModule from "../../../app/filelist/FileListModule.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const withPortalsComp = mockComponent(mockFunction(), "FileListBrowser");

describe("FileListRoot.test.mjs", () => {
  it("should render component with contexts", async () => {
    //given
    const db = await testDb();
    const [historyProviderCtx, fsCtx, servicesComp] = getServicesCtxHook();
    FileListRoot.fileListComp = servicesComp;
    const dispatch = mockFunction();
    const module = new FileListModule(db);
    const rootComp = FileListRoot(dispatch, module, withPortalsComp);

    //when
    const result = TestRenderer.create(h(rootComp, null, "test_child")).root;

    //then
    deepEqual(historyProviderCtx.current === module.historyProvider, true);
    deepEqual(fsCtx.current === module.fsServices, true);
    assertComponents(
      result.children,
      h(
        withPortalsComp,
        null,
        h(servicesComp, { dispatch, isRightInitiallyActive: false }),
        "test_child",
      ),
    );
  });
});

/**
 * @returns {[
 *  React.MutableRefObject<HistoryProvider | null>,
 *  React.MutableRefObject<FSServices | null>,
 *  {
 *    (props: FileListBrowserProps): React.FunctionComponentElement<WithStacksProps>;
 *    displayName: string;
 *  }
 * ]}
 */
function getServicesCtxHook() {
  /** @type {React.MutableRefObject<HistoryProvider | null>} */
  const historyProviderRef = React.createRef();
  /** @type {React.MutableRefObject<FSServices | null>} */
  const fsRef = React.createRef();
  const comp = () => {
    historyProviderRef.current = HistoryProvider.useHistoryProvider();
    fsRef.current = React.useContext(FSServices.Context);
    return h(React.Fragment, null);
  };
  comp.displayName = "test_comp";

  //@ts-ignore
  return [historyProviderRef, fsRef, comp];
}
