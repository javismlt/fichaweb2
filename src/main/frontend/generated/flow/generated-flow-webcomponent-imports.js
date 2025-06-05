import { injectGlobalWebcomponentCss } from 'Frontend/generated/jar-resources/theme-util.js';

import { injectGlobalCss } from 'Frontend/generated/jar-resources/theme-util.js';

import { css, unsafeCSS, registerStyles } from '@vaadin/vaadin-themable-mixin';
import $cssFromFile_0 from 'Frontend/themes/my-theme/styles.css?inline';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/text-field/theme/lumo/vaadin-text-field.js';
import '@vaadin/password-field/theme/lumo/vaadin-password-field.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/horizontal-layout/theme/lumo/vaadin-horizontal-layout.js';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import '@vaadin/notification/theme/lumo/vaadin-notification.js';
import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';
const $css_0 = typeof $cssFromFile_0  === 'string' ? unsafeCSS($cssFromFile_0) : $cssFromFile_0;
registerStyles('vaadin-grid', $css_0, {moduleId: 'flow_css_mod_0'});

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '9897c35ad5086e8ac9a0211b5ad2ff44c0052cf7372c34864820686e63f23be8') {
    pending.push(import('./chunks/chunk-ea436abe66817334f08d659a768b327903240631313ef78f191caac1a5249098.js'));
  }
  if (key === 'e48e52035f5d6ee4ccbcdd1bd16d446c9ff418280ed80c8c41223d59048ff3b9') {
    pending.push(import('./chunks/chunk-faa7c159f31daf8e01614d9c5c59f580ba45662aefacb8b9b0df0c75a79154c0.js'));
  }
  if (key === '0ec24de11f42d67234e3de6f888e7185d5582bafa943392ece39f3c4217400e1') {
    pending.push(import('./chunks/chunk-84f33eff0b97191b35f2127762d0d2e4756746a4d4465806472267c25b12d5ba.js'));
  }
  if (key === '5f6f0b06c85c7a9776ac5f0df5c4b87ee340b8b450f436f74833e71b38f1e7bb') {
    pending.push(import('./chunks/chunk-faa7c159f31daf8e01614d9c5c59f580ba45662aefacb8b9b0df0c75a79154c0.js'));
  }
  if (key === '1369c0ebf61a2e6f607cc442b600499e0816c12ce6ad5eed4a3d53232f4f1df0') {
    pending.push(import('./chunks/chunk-e48da8645c61f470c0962dc663ca7ecc4968adf55cdc8618cd5576071f66f706.js'));
  }
  if (key === '6aac36ca9c0040e978a04990228f007beec31bd8635891c87c91476ca3b6dced') {
    pending.push(import('./chunks/chunk-3c582a00fe6aa703cc2e6510da782fa5d22d179f806d335a67bd917bebc81ff4.js'));
  }
  if (key === '5847d793d152d61f0f86f288bcf742d17138374da91b0ebe734afd7a5186009a') {
    pending.push(import('./chunks/chunk-b9ce5c3f94d9f4b44c7a7e349b035f42d62a741b987d581aff4d5c5a12533fc8.js'));
  }
  if (key === 'fafdfa2b8a29f9b79cdb7909491d1477d4e7e8194914c18e93491f58e0071f5c') {
    pending.push(import('./chunks/chunk-1bb2019748c6e6c9010a254c2bf437d7e70a5282fb812eb51dbd37377642098d.js'));
  }
  if (key === '2f1e9495d2f9fd6c2497fe3184dd38723519e8f75c53a4ed7aa128992372ce6a') {
    pending.push(import('./chunks/chunk-b9ce5c3f94d9f4b44c7a7e349b035f42d62a741b987d581aff4d5c5a12533fc8.js'));
  }
  if (key === '0978ddf81bc4eb08731e586f52d1b22f4a193db60ccb8aa0020be5ee203aaf91') {
    pending.push(import('./chunks/chunk-d9c883def7a701564b8b6f85f103154d3eadb0aa71c1c2e7ae21c2172f1e7154.js'));
  }
  if (key === 'd8dc63834ed380e57fcf598c8b6c643e56de5e0e18b76a591ff3beb47b4c8da3') {
    pending.push(import('./chunks/chunk-3828d3bf8e547a88082caa0379cc522bdc46020e8f8164787410eb518855f95d.js'));
  }
  if (key === '258624f1b2c3441ad54125cfa6585485745f45c84b0081cf6a2e93ef6b15d40c') {
    pending.push(import('./chunks/chunk-1d9f7667e620a32b2c92e50021671ffd20c10726d803160bfe2fcc657d852094.js'));
  }
  if (key === 'd8d8faa98ad7b27908673fc966c455a205d535fd0c1e281e78028721e5c18af7') {
    pending.push(import('./chunks/chunk-faa7c159f31daf8e01614d9c5c59f580ba45662aefacb8b9b0df0c75a79154c0.js'));
  }
  if (key === 'b1c409cb1bc0d91ce16a6e26afc4a3a01f624f9bf829766cc1a831d3f4e13805') {
    pending.push(import('./chunks/chunk-f69006e328bd46f9d629b7dc6ffb776a8b18aab195babe20ab8ca2fb91d500b0.js'));
  }
  if (key === 'e06ece419e2104336be54b10ace3777bccedda89130c746ccc347c402e0a60db') {
    pending.push(import('./chunks/chunk-1e909fb0d632be93c3fc2b819857c69a7bf035c9d1eabb2a5bf67fbb93202abd.js'));
  }
  if (key === '555a85d8d55b647990c39bb718d2e4463d448ad29dc9a240b966a4e183b66bb2') {
    pending.push(import('./chunks/chunk-3d48902cfbeb4ecb254d30edd3385eb0001eeb8dbf4d863b2b3494a7471f8515.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}