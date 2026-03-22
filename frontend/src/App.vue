<script setup>
import { ref } from "vue";
import GatewayWorkspace from "./components/GatewayWorkspace.vue";
import MonitoringWorkspace from "./components/MonitoringWorkspace.vue";
import PaymentWorkspace from "./components/PaymentWorkspace.vue";

const workspaceTab = ref("gateway");
</script>

<template>
  <div class="app-shell">
    <header class="topbar card">
      <div>
        <p class="eyebrow">ToB Fiat Payment Platform</p>
        <h1>Workspace Control Panel</h1>
        <p class="muted">
          当前把多通道网关、路由监控、payment 审批流先放在同一个前端里，便于快速联调。
        </p>
      </div>
      <div class="topbar-summary">
        <span class="badge">Gateway</span>
        <span class="badge">Routing</span>
        <span class="badge">Monitoring</span>
        <span class="badge">Payment Workflow</span>
      </div>
    </header>

    <div class="workspace-tabs">
      <button type="button" class="tab-button" :class="{ active: workspaceTab === 'gateway' }" @click="workspaceTab = 'gateway'">
        Channel Gateway
      </button>
      <button type="button" class="tab-button" :class="{ active: workspaceTab === 'routing' }" @click="workspaceTab = 'routing'">
        Routing & Monitoring
      </button>
      <button type="button" class="tab-button" :class="{ active: workspaceTab === 'payment' }" @click="workspaceTab = 'payment'">
        Payment Module
      </button>
    </div>

    <GatewayWorkspace v-if="workspaceTab === 'gateway'" />
    <MonitoringWorkspace v-if="workspaceTab === 'routing'" />
    <PaymentWorkspace v-if="workspaceTab === 'payment'" />
  </div>
</template>
