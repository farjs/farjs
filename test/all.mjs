await import("./app/service/HistoryProviderImpl.test.mjs");
await import("./app/service/HistoryServiceImpl.test.mjs");

await import("./dao/FolderShortcutDao.test.mjs");
await import("./dao/HistoryDao.test.mjs");
await import("./dao/HistoryKindDao.test.mjs");

await import("./file/Encoding.test.mjs");
await import("./file/FileEvent.test.mjs");
await import("./file/FilePlugin.test.mjs");
await import("./file/FilePluginUi.test.mjs");
await import("./file/FileReader.test.mjs");
await import("./file/FileViewHistory.test.mjs");
await import("./file/MockFileReader.test.mjs");
await import("./file/popups/EncodingsPopup.test.mjs");
await import("./file/popups/FileViewHistoryController.test.mjs");
await import("./file/popups/FileViewHistoryPopup.test.mjs");
await import("./file/popups/TextSearchPopup.test.mjs");

await import("./filelist/popups/DeleteController.test.mjs");
await import("./filelist/popups/ExitController.test.mjs");
await import("./filelist/popups/HelpController.test.mjs");
await import("./filelist/popups/MakeFolderController.test.mjs");
await import("./filelist/popups/MakeFolderPopup.test.mjs");
await import("./filelist/popups/MenuController.test.mjs");
await import("./filelist/popups/SelectController.test.mjs");
await import("./filelist/popups/SelectPopup.test.mjs");
await import("./filelist/FileListUi.test.mjs");
await import("./filelist/FileListUiPlugin.test.mjs");

await import("./fs/popups/FolderShortcutsController.test.mjs");
await import("./fs/popups/FolderShortcutsPopup.test.mjs");
await import("./fs/popups/FolderShortcutsService.test.mjs");
await import("./fs/popups/MockFolderShortcutsService.test.mjs");
await import("./fs/FSServices.test.mjs");

await import("./viewer/ViewerFileReader.test.mjs");
await import("./viewer/ViewerHeader.test.mjs");
await import("./viewer/ViewerInput.test.mjs");
await import("./viewer/ViewerSearch.test.mjs");
await import("./viewer/ViewItemsPopup.test.mjs");
await import("./viewer/quickview/QuickViewDir.test.mjs");
