<template>
  <div class="modal is-active">
    <div class="modal-background" />
    <div class="modal-card">
      <div class="modal-card-head">
        <slot name="header" />
      </div>

      <div class="modal-card-body">
        <slot name="content" />
      </div>
      <div class="modal-card-foot is-justify-content-space-between">
        <div>
          <slot name="actions" />
        </div>
        <div>
          <slot name="footer" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts"></script>
