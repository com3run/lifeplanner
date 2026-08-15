import { defineConfig } from "tsup";

// esbuild-based build (aligns with what Claude Design's converter expects): one ESM
// entry + type declarations, React kept external.
export default defineConfig({
  entry: ["src/index.ts"],
  format: ["esm"],
  dts: true,
  clean: true,
  external: ["react", "react-dom"],
  target: "es2020",
});
