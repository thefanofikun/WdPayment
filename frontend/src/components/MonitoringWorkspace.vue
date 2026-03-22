<script setup>
import { onMounted, ref } from "vue";

const routeOperation = ref("PAYOUT");
const routingRows = ref([]);
const routeHistoryRows = ref([]);
const monitoringRows = ref([]);
const monitoringSignals = ref([]);
const monitoringError = ref("");

onMounted(async () => {
  await Promise.all([loadRouting(), loadMonitoring(), loadRouteHistory()]);
});

async function requestJson(url) {
  const response = await fetch(url);
  const body = await response.json();
  if (!response.ok || !body.success) {
    throw new Error(body.message || "Request failed");
  }
  return body.data;
}

async function loadRouting() {
  try {
    monitoringError.value = "";
    const data = await requestJson(`/api/routing/recommendations?operation=${encodeURIComponent(routeOperation.value)}`);
    routingRows.value = data.recommendations ?? [];
    await loadRouteHistory();
  } catch (error) {
    monitoringError.value = error.message;
  }
}

async function loadRouteHistory() {
  try {
    const data = await requestJson(`/api/routing/history?operation=${encodeURIComponent(routeOperation.value)}`);
    routeHistoryRows.value = data.history ?? [];
  } catch (error) {
    monitoringError.value = error.message;
  }
}

async function loadMonitoring() {
  try {
    monitoringError.value = "";
    const data = await requestJson("/api/monitoring/channels");
    monitoringRows.value = data.metrics ?? [];
    monitoringSignals.value = data.monitoredSignals ?? [];
  } catch (error) {
    monitoringError.value = error.message;
  }
}
</script>

<template>
  <section class="content-stack">
    <section class="card content-card">
      <div class="section-header">
        <div>
          <p class="eyebrow">Routing Ranking</p>
          <h2>Success-rate driven routing</h2>
        </div>
        <div class="toolbar">
          <select v-model="routeOperation" class="input compact-input" @change="loadRouting">
            <option value="CUSTOMER_ONBOARDING">CUSTOMER_ONBOARDING</option>
            <option value="VIRTUAL_ACCOUNT">VIRTUAL_ACCOUNT</option>
            <option value="BENEFICIARY">BENEFICIARY</option>
            <option value="PAYOUT">PAYOUT</option>
            <option value="WEBHOOK">WEBHOOK</option>
          </select>
          <button type="button" class="secondary-button" @click="loadRouting">Refresh Ranking</button>
        </div>
      </div>

      <div class="table-shell">
        <table class="data-table">
          <thead>
            <tr>
              <th>Rank</th>
              <th>Channel</th>
              <th>Success Rate</th>
              <th>Avg Latency</th>
              <th>Total</th>
              <th>Reason</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in routingRows" :key="`${row.operation}-${row.channelCode}`">
              <td>{{ row.rank }}</td>
              <td>{{ row.channelCode }} / {{ row.channelName }}</td>
              <td>{{ row.successRate.toFixed(2) }}%</td>
              <td>{{ row.averageLatencyMs }} ms</td>
              <td>{{ row.totalCount }}</td>
              <td>{{ row.recommendationReason }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="card content-card">
      <div class="section-header">
        <div>
          <p class="eyebrow">Route History</p>
          <h2>Persisted routing decisions</h2>
        </div>
        <button type="button" class="secondary-button" @click="loadRouteHistory">Refresh History</button>
      </div>

      <div class="table-shell">
        <table class="data-table">
          <thead>
            <tr>
              <th>Created At</th>
              <th>Operation</th>
              <th>Rank</th>
              <th>Channel</th>
              <th>Success Rate</th>
              <th>Avg Latency</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in routeHistoryRows" :key="row.id">
              <td>{{ row.createdAt }}</td>
              <td>{{ row.operation }}</td>
              <td>{{ row.routeRank }}</td>
              <td>{{ row.channelCode }} / {{ row.channelName }}</td>
              <td>{{ row.successRate.toFixed(2) }}%</td>
              <td>{{ row.averageLatencyMs }} ms</td>
              <td>{{ row.totalCount }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="card content-card">
      <div class="section-header">
        <div>
          <p class="eyebrow">Channel Monitoring</p>
          <h2>Health signals</h2>
          <p class="muted">
            这里同时展示实时内存指标，以及已经沉淀到 ops_db 的快照基础。
          </p>
        </div>
        <button type="button" class="secondary-button" @click="loadMonitoring">Refresh Metrics</button>
      </div>

      <div class="badge-row">
        <span v-for="signal in monitoringSignals" :key="signal" class="badge soft">{{ signal }}</span>
      </div>

      <p v-if="monitoringError" class="error-text">{{ monitoringError }}</p>

      <div class="table-shell">
        <table class="data-table">
          <thead>
            <tr>
              <th>Operation</th>
              <th>Channel</th>
              <th>Success Rate</th>
              <th>Failures</th>
              <th>Avg Latency</th>
              <th>Last Status</th>
              <th>Last Message</th>
              <th>Updated At</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in monitoringRows" :key="`${row.operation}-${row.channelCode}`">
              <td>{{ row.operation }}</td>
              <td>{{ row.channelCode }}</td>
              <td>{{ row.successRate.toFixed(2) }}%</td>
              <td>{{ row.failureCount }}</td>
              <td>{{ row.averageLatencyMs }} ms</td>
              <td>{{ row.lastStatus }}</td>
              <td>{{ row.lastMessage }}</td>
              <td>{{ row.lastUpdatedAt }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
