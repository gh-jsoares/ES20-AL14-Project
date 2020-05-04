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
    <v-card class="mx-auto  py-sm-5 px-sm-8" max-width="1000px">
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
        <v-row class="mt-5 hidden-sm-and-down">
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
        <!-- start of mobile -->
        <v-row class="hidden-md-and-up">
          <v-col>
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
                ></v-text-field>
              </template>
            </v-slider>
          </v-col>
        </v-row>
        <v-row class="hidden-md-and-up">
          <v-switch
            class="my-0 mx-auto"
            v-model="tourn.scramble"
            label="Scramble"
          ></v-switch>
        </v-row>
        <!-- end of mobile -->
        <v-row>
          <v-col>
            <VueCtkDateTimePicker
              label="*Start Date"
              v-model="tourn.availableDate"
              id="startDateInput"
              format="YYYY-MM-DDTHH:mm:ssZ"
              data-cy="startDate"
            ></VueCtkDateTimePicker>
          </v-col>
          <v-col>
            <VueCtkDateTimePicker
              label="*Conclusion Date"
              v-model="tourn.conclusionDate"
              id="endDateInput"
              format="YYYY-MM-DDTHH:mm:ssZ"
              data-cy="endDate"
            ></VueCtkDateTimePicker>
          </v-col>
        </v-row>
        <v-row>
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
      !this.tourn.availableDate ||
      !this.tourn.conclusionDate
    ) {
      this.errorMsg = 'Missing required fields';
      return false;
    }
    if (Date.parse(this.tourn.availableDate) < Date.now()) {
      this.errorMsg = 'Start Date must be in the future';
      return false;
    }
    if (
      Date.parse(this.tourn.availableDate) >
      Date.parse(this.tourn.conclusionDate)
    ) {
      this.errorMsg = 'Conclusion Date must be after Start Date';
      return false;
    }
    return true;
  }

  show(created: Tournament) {
    this.saved = true;
    this.successMsg =
      'New tournament Saved: ' +
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
  }
}
</script>

<style scoped>
.slider-helper >>> input {
  width: 40px;
  text-align: center;
}
</style>
