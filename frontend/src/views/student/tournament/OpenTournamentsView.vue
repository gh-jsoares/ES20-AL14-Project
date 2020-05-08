<template>
  <div>
    <!--mobile layout-->
    <div class="hidden-md-and-up pa-2" style="overflow-x: hidden">
      <v-text-field
        placeholder="Search by Title"
        append-icon="search"
        v-model="search"
        @keyup="filterTournaments()"
        hide-details
        single-line
        solo
        dense
      ></v-text-field>
      <h2 class="mt-5">Open Tournaments</h2>
      <v-dialog v-model="dialog" max-width="350">
        <v-card class="mx-auto text-left">
          <v-card-text class="pa-4">
            <p class="overline mb-4">{{ getStatus(current) }}</p>
            <p class="title text-left text--darken-1 mb-1">
              {{ current.title }}
            </p>
            <p class="subtitle-2 overline mt-1 mb-4">
              By {{ current.creator == null ? '' : current.creator.username }}
            </p>
            <v-chip
              class="ma-1"
              label
              v-for="topic in current.topics"
              :key="topic.id"
              >{{ topic.name }}</v-chip
            >
            <p class="mt-4 ma-0">
              Has {{ current.numberOfQuestions }} questions and there's
              {{ current.numberOfEnrolls }} students currently enrolled. Is
              open:
            </p>
            <v-spacer></v-spacer>
            <p class="text-center mt-1 ma-0">
              From: {{ current.availableDate }}
            </p>
            <p class="text-center ma-0">To: {{ current.conclusionDate }}</p>
          </v-card-text>
          <v-card-actions>
            <v-btn color="grey darken-2" @click="dialog = false" text>
              Close
            </v-btn>
            <v-spacer></v-spacer>
            <v-btn
              v-if="getStatus(current) === 'Not Started'"
              color="success"
              :disabled="current.userEnrolled"
              @click="enrollTournament(current)"
              >Enroll</v-btn
            >
            <v-btn
              v-else-if="
                getStatus(current) === 'Started' && current.statementQuiz
              "
              color="primary"
              class="solveQuiz"
              @click="solveQuiz(current.statementQuiz)"
              data-cy="solveQuiz"
            >
              Solve
            </v-btn>
            <v-spacer></v-spacer>
            <v-btn
              v-if="
                getStatus(current) === 'Not Started' &&
                  checkIsCreator(current.creator)
              "
              right
              color="red"
              @click="cancelTournament(current)"
              text
            >
              Delete
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
      <v-card
        :class="
          'px-3 my-2 py-0 ' +
            (tourn.state === 'ENROLL' ? 'not-started' : 'started')
        "
        v-for="tourn in listTourns"
        :key="tourn.id"
        @click="openDialog(tourn)"
      >
        <v-row justify="center">
          <v-col class="text-left">
            {{ tourn.title }}
          </v-col>
          <v-col cols="2"
            ><v-icon color="primary" dense
              >fas fa-chevron-circle-right</v-icon
            ></v-col
          >
        </v-row>
      </v-card>
    </div>
    <!--end mobile layout-->
    <div class="container hidden-sm-and-down">
      <v-text-field
        class="searchBar"
        placeholder="Search by Title"
        append-icon="search"
        v-model="search"
        @keyup="filterTournaments()"
        hide-details
        single-line
        solo
        dense
        data-cy="searchBar"
      ></v-text-field>
      <h2>Open Tournaments</h2>
      <ul data-cy="tournTable">
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
          <div class="col short-col"></div>
        </li>
        <li
          class="list-row"
          v-for="tourn in listTourns"
          :key="tourn.id"
          data-cy="tournRow"
        >
          <div class="col">
            {{ tourn.title }}
          </div>
          <div class="col">
            {{ tourn.creator == null ? '' : tourn.creator.username }}
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
          <div class="col short-col" data-cy="numEnrolls">
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
          <div class="col">
            <v-btn
              v-if="getStatus(tourn) === 'Not Started'"
              :disabled="tourn.userEnrolled"
              color="success"
              class="enroll"
              outlined
              @click="enrollTournament(tourn)"
              data-cy="enrollBtn"
            >
              Enroll
            </v-btn>
            <v-btn
              v-else-if="getStatus(tourn) === 'Started'"
              :disabled="!tourn.userEnrolled || tourn.statementQuiz.completed"
              width="90"
              color="primary"
              class="solveQuiz"
              @click="solveQuiz(tourn.statementQuiz)"
              data-cy="solveQuizBtn"
            >
              Solve
            </v-btn>
          </div>
          <div class="col short-col last-col">
            <v-tooltip
              bottom
              v-if="
                getStatus(tourn) === 'Not Started' &&
                  checkIsCreator(tourn.creator)
              "
            >
              <template v-slot:activator="{ on }">
                <v-icon
                  large
                  color="red"
                  @click="cancelTournament(tourn)"
                  data-cy="cancelBtn"
                  v-on="on"
                >
                  mdi-delete-forever
                </v-icon>
              </template>
              <span>Delete</span>
            </v-tooltip>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Tournament } from '@/models/management/Tournament';
import User from '@/models/user/User';
import StatementQuiz from '@/models/statement/StatementQuiz';
import StatementManager from '@/models/statement/StatementManager';

@Component
export default class OpenTournamentsView extends Vue {
  tournaments: Tournament[] = [];
  listTourns: Tournament[] = [];
  tournamentsEnrolledId: number[] = [];
  search: String = '';
  dialog: boolean = false;
  current: Tournament = new Tournament();
  async created() {
    this.current.creator = new User();
    await this.$store.dispatch('loading');
    try {
      this.tournaments = await RemoteServices.getOpenTournaments();
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
    await this.$store.dispatch('loading');
    try {
      response = await RemoteServices.enrollTournament(tournament.id);
      tournament.numberOfEnrolls = response.numberOfEnrolls;
      tournament.userEnrolled = response.userEnrolled;
      this.tournamentsEnrolledId.push(response.id);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async solveQuiz(quiz: StatementQuiz) {
    let statementManager: StatementManager = StatementManager.getInstance;
    statementManager.statementQuiz = quiz;
    await this.$router.push({ name: 'solve-quiz' });
  }

  async cancelTournament(tournament: Tournament) {
    if (
      tournament.id &&
      confirm('Are you sure you want to delete this question?')
    ) {
      try {
        await RemoteServices.cancelTournament(tournament.id);
        this.listTourns = this.listTourns.filter(
          tour => tour.id != tournament.id
        );
        this.dialog = false;
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  checkIsCreator(user: User): boolean {
    if (this.$store.getters.getUser == null || user == null) return false;
    return user.username == this.$store.getters.getUser.username;
  }

  openDialog(tourn: Tournament) {
    this.dialog = true;
    this.current = tourn;
  }
}
</script>

<style lang="scss" scoped>
.started {
  border-left: 12px solid limegreen !important;
}

.not-started {
  border-left: 12px solid white !important;
}

.container {
  max-width: 90vw;
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
