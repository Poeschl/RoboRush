/// <reference types="vite/client" />
import "vue-router";

// To ensure it is treated as a module, add at least one `export` statement
export {};

declare module "vue-router" {
  interface RouteMeta {
    requiresNonAdmin?: boolean;
    hideFooter?: boolean;
    requiresAdmin?: boolean;
    hideNavBar?: boolean;
    noContainer?: boolean;
  }
}
