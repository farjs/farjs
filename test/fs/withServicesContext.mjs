/**
 * @typedef {import("../../fs/FSServices.mjs").FSServices} FSServices
 */
import React from "react";
import FSServices from "../../fs/FSServices.mjs";

const h = React.createElement;

/**
 * @param {React.ReactElement} element
 * @param {FSServices} fsServices
 * @returns {React.ReactElement}
 */
const withServicesContext = (element, fsServices) => {
  return h(FSServices.Context.Provider, { value: fsServices }, element);
};

export default withServicesContext;
