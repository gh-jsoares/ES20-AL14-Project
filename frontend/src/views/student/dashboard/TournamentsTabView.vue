<template>
  <div class="container">
    <h2>Tournaments Dashboard</h2>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { TournamentDashStats } from '@/models/management/TournamentDashStats';

@Component
export default class TournamentsTabView extends Vue {
  stats!: TournamentDashStats;

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.stats = await RemoteServices.getTournamentsDashboardStats();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
}
</script>

<style scoped></style>
