import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";

export default FileListPluginLoader(["S-f7"], async () => {
  const module = "./ArchiverPlugin.mjs";
  return (await import(module)).default;
});
