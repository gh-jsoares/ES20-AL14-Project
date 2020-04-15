<template id="createTournApp">
  <div class="container">
    <v-alert
      v-model="saved"
      transition="slide-y-transition"
      type="success"
      close-text="Close Alert"
      dismissible
      data-cy="saved"
    >
      {{ successMsg }}
    </v-alert>
    <v-alert
      v-model="failed"
      transition="slide-y-transition"
      type="error"
      close-text="Close Alert"
      dismissible
      data-cy="failed"
    >
      {{ errorMsg }}
    </v-alert>
    <v-card class="mx-auto  py-5 px-8" max-width="1000px">
      <v-card-title>Create Tournament</v-card-title>
      <v-spacer></v-spacer>
      <v-card-text>
        <v-text-field v-model="tourn.title" label="*Title" data-cy="Title" />
        <v-autocomplete
          v-model="tourn.topics"
          :items="topics"
          chips
          label="*Topics"
          hide-details
          hide-no-data
          hide-selected
          multiple
          deletable-chips
          data-cy="Topics"
        ></v-autocomplete>
        <v-row class="mt-5">
          <v-col cols="9">
            <v-subheader>Number of Questions</v-subheader>
            <v-slider
              v-model="tourn.numberOfQuestions"
              thumb-label
              :min="1"
              :max="100"
              persistent-hint
              data-cy="QuestSlider"
            >
              <template v-slot:prepend>
                <v-text-field
                  v-model="tourn.numberOfQuestions"
                  class="mt-0 pt-0 slider-helper"
                  :min="1"
                  :max="100"
                  dense
                  height="23px"
                  data-cy="QuestText"
                ></v-text-field>
              </template>
            </v-slider>
          </v-col>
          <v-col
            style="display: flex; justify-content: center; align-self: center"
          >
            <v-switch
              class="pt-6"
              v-model="tourn.scramble"
              label="Scramble"
              data-cy="Scramble"
            ></v-switch>
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            <v-datetime-picker
              label="*Start Date"
              v-model="startDate"
              format="yyyy-MM-dd HH:mm"
              date-format="yyyy-MM-dd"
              time-format="HH:mm"
              data-cy="startDate"
            >
            </v-datetime-picker>
          </v-col>
          <v-col>
            <v-datetime-picker
              label="*Conclusion Date"
              v-model="endDate"
              format="yyyy-MM-dd HH:mm"
              date-format="yyyy-MM-dd"
              time-format="HH:mm"
              data-cy="endDate"
            >
            </v-datetime-picker>
          </v-col>
        </v-row>
        <v-row class="mt-5">
          <v-spacer></v-spacer>
          <v-btn color="primary" @click="save()" data-cy="createBtn"
            >Create</v-btn
          >
        </v-row>
      </v-card-text>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Tournament } from '@/models/management/Tournament';

@Component
export default class CreateTournamentView extends Vue {
  tourn: Tournament = new Tournament();
  topics: object[] = [];
  saved: boolean = false;
  failed: boolean = false;
  successMsg: string = '';
  errorMsg: string = '';
  startDate: string | undefined;
  endDate: string | undefined;
  async created() {
    await this.$store.dispatch('loading');
    try {
      let response = await RemoteServices.getTopics();
      for (let topic of response)
        this.topics.push({ text: topic.name, value: topic });
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async save() {
    if (this.valid()) this.failed = false;
    else {
      this.failed = true;
      return;
    }
    this.tourn.availableDate = this.format(this.startDate);
    this.tourn.conclusionDate = this.format(this.endDate);
    try {
      let created = await RemoteServices.createTournament(this.tourn);
      this.show(created);
      this.clear();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  valid(): boolean {
    if (
      this.tourn.topics.length <= 0 ||
      !this.tourn.title ||
      !this.startDate ||
      !this.endDate
    ) {
      this.errorMsg = 'Missing required fields';
      return false;
    }
    if (Date.parse(this.startDate) < Date.now()) {
      this.errorMsg = 'Start Date must be in the future';
      return false;
    }
    if (Date.parse(this.startDate) > Date.parse(this.endDate)) {
      this.errorMsg = 'Conclusion Date must be after Start Date';
      return false;
    }
    return true;
  }

  show(created: Tournament) {
    this.saved = true;
    this.successMsg =
      'New Tournament Saved: ' +
      created.title +
      ' by ' +
      created.creator?.username +
      ' with ' +
      created.numberOfQuestions +
      ' questions, open from ' +
      created.availableDate +
      ' to ' +
      created.conclusionDate;
  }

  format(date: string | undefined): string {
    if (date === undefined) return '';
    return new Date(date)
      .toISOString()
      .replace('T', ' ')
      .substr(0, 16);
  }

  clear() {
    this.tourn.title = '';
    this.tourn.topics = [];
    this.tourn.numberOfQuestions = 1;
    this.tourn.scramble = false;
    this.tourn.availableDate = '';
    this.tourn.conclusionDate = '';
    this.startDate = '';
    this.endDate = '';
  }
}
</script>

<style scoped>
.slider-helper >>> input {
  width: 40px;
  text-align: center;
}
</style>
