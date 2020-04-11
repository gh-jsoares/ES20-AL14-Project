<template>
  <div class="container">
    <v-text-field
      class="searchBar"
      placeholder="Search by Title"
      append-icon="search"
      v-model="search"
      @keyup="filterTournaments()"
      hide-details
      single-line
      rounded
      solo
      dense
    ></v-text-field>
    <h2>Available Tournaments</h2>
    <ul>
      <li class="list-header">
        <div class="col">Title</div>
        <div class="col">Creator</div>
        <div class="col long-col">Topics</div>
        <div class="col short-col">Questions</div>
        <div class="col short-col">Enrolled</div>
        <div class="col">Opens At</div>
        <div class="col">Closes At</div>
        <div class="col">Status</div>
        <div class="col last-col"></div>
      </li>
      <li class="list-row" v-for="tourn in listTourns" :key="tourn.id">
        <div class="col">
          {{ tourn.title }}
        </div>
        <div class="col">
          {{ tourn.creator.username }}
        </div>
        <div class="col long-col">
          <v-chip
            class="topic"
            v-for="topic in tourn.topics"
            :key="topic.id"
            label
          >
            {{ topic.name }}
          </v-chip>
        </div>
        <div class="col short-col">
          {{ tourn.numberOfQuestions }}
        </div>
        <div class="col short-col">
          {{ tourn.numberOfEnrolls }}
        </div>
        <div class="col">
          {{ tourn.availableDate }}
        </div>
        <div class="col">
          {{ tourn.conclusionDate }}
        </div>
        <div class="col">
          {{ getStatus(tourn) }}
        </div>
        <div class="col last-col">
          <i class="fas fa-chevron-circle-right"></i>
        </div>
      </li>
    </ul>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import StatementManager from '@/models/statement/StatementManager';
import StatementQuiz from '@/models/statement/StatementQuiz';
import StatementQuestion from '@/models/statement/StatementQuestion';
import StatementAnswer from '@/models/statement/StatementAnswer';
import { Tournament } from '@/models/management/Tournament';
import Topic from '@/models/management/Topic';

@Component
export default class OpenTournamentsView extends Vue {
  tournaments: Tournament[] = [];
  listTourns: Tournament[] = [];
  tournamentsEnrolledId: number[] = [];
  search: String = '';
  async created() {
    await this.$store.dispatch('loading');
    try {
      this.tournaments = (await RemoteServices.getOpenTournaments()).reverse();
      this.listTourns = this.tournaments.slice();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  filterTournaments() {
    const match = this.search.toLowerCase();
    this.listTourns = this.tournaments.filter(tourn =>
      tourn.title.toLowerCase().includes(match)
    );
  }

  getStatus(tourn: Tournament): string {
    switch (tourn.state) {
      case 'ENROLL':
        return 'Not Started';
      case 'ONGOING':
        return 'Started';
      default:
        return '?';
    }
  }

  async enrollTournament(tournament: Tournament) {
    let response;
    try {
      response = await RemoteServices.enrollTournament(tournament.id);
      this.tournamentsEnrolledId.push(response.id);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
}
</script>

<style lang="scss" scoped>
.container {
  max-width: 75vw;
  margin-left: auto;
  margin-right: auto;
  padding-left: 10px;
  padding-right: 10px;

  .searchBar {
    width: 500px;
    margin-top: 10px;
  }

  h2 {
    font-size: 26px;
    margin: 20px 0;
    text-align: center;
    small {
      font-size: 0.5em;
    }
  }

  ul {
    overflow: hidden;
    padding: 0 5px;

    li {
      border-radius: 3px;
      padding: 5px 10px;
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
    }

    .list-header {
      background-color: #1976d2;
      color: white;
      font-size: 14px;
      text-transform: uppercase;
      letter-spacing: 0.03em;
      text-align: center;
    }

    .col {
      flex-basis: 25% !important;
      margin: auto; /* Important */
      text-align: center;
    }

    .list-row {
      background-color: #ffffff;
      box-shadow: 0 0 9px 0 rgba(0, 0, 0, 0.1);
      display: flex;
    }

    .short-col {
      flex-basis: 15% !important;
    }

    .long-col {
      flex-basis: 40% !important;
    }

    .topic {
      margin: 3px;
    }
  }
}
</style>
