# Murdle Solver

This repo contains two complementary apps ([adk-svc](./adk-svc/) and [android-app](./android-app/)) that are able to solve (most of) the jumbled word puzzles in [Murdle: Volume 1](https://www.amazon.ca/Murdle-Elementary-Impossible-Mysteries-Deduction/dp/1250892317/ref=sr_1_1?dib=eyJ2IjoiMSJ9.r5bGp0JssPfYplrmNFDXmbISWlyiFdoXoh8d62YyWi17DJxtZGBpfSm138DKeeNEleHb6Sp1w0TFcV8ZEcTgqx9Ydev1sGgKNxW9FWD1PABofWwx3H3gBKZ9T14nLU8wmz9xkycWffnVZKR4DqwhTq60dT8EoNmXTQ-BrDr8Zr9ZIBAplY6YyLCpXkKoykQG3romH3hBQXueHkuFWTdPe6bXxXPtawYSBYd2II9by8u0zgIhfQRpB_Pvj-koaFSGh59U402V0fD1XN94KQnHer44YZAgtp0SsmP_q0h5S4w.kg1OEmnML6MVDzja4iuQ8F_2SdTC4MG-QLgOyRfEtIw&dib_tag=se&hvadid=657154094381&hvdev=c&hvlocphy=9001618&hvnetw=g&hvqmt=e&hvrand=6763604865215210698&hvtargid=kwd-2085859184561&hydadcr=325_1014995618&keywords=murder+volume+1&mcid=fd372daa4d1c370ca91fdd5a0f74db73&qid=1747611832&sr=8-1).

## Building and Running
In order to try this out, you will need to deploy the [adk-svc](./adk-svc) service, and then build and deploy the [android-app](./android-app/) service.

### adk-svc
This app was built using Python 3.11.6 and google-adk==0.5.0. It's recommended you create a `virtualenv` environment and install the requirements, like so:

```bash
$ cd adk-svc
$ python -m virtualenv .venv
$ source .venv/bin/activate
$ pip install -r ./murdle_solver_agent/requirements.txt
```

You will need to authenticate against Google Cloud to either perform local testing or deploy the application to Cloud Run. Update the variables in `./adk-svc/murdle_solver_agent/.env` according to your specific needs. 

**Note:** Do not change the `GOOGLE_CLOUD_LOCATION` variable arbitrarily, as `gemini-2.5-pro-preview-05-06` (the LLM that backs the ADK agents used) may not be available in all regions. Check to see if the model exists in your desired region, or change the model as needed.

You will also need to install the [gcloud CLI utility](https://cloud.google.com/sdk/docs/install) and execute the following command to authenticate:

```bash
$ gcloud auth application-default login
```

#### Local Testing
You can run the built-in FastAPI server using the adk api_server command from the `adk-svc` directory:
```bash
adk api_server
```

You can also interactively play with the agents, view state, and transitions via the frontend web application
```bash
adk web
```

#### Deployment
This app can be configured to run on Google Cloud run. You will need a Google Cloud project set up, with Vertex AI and Cloud Run enabled.

Run the following once that's been taken care of:

```bash
source ./murdle_solver_agent/.env
adk deploy cloud_run --project=$GOOGLE_CLOUD_PROJECT --region=$GOOGLE_CLOUD_LOCATION
```

I chose to allow public authentication and a bunch of other stuff that made my life easier during this build. **The android application assumes no authentication exists and will need to be updated to work with an authentication pattern if you choose differently**.

### android-app
I recommend installing [Android Studio](https://developer.android.com/studio), importing the [./android-app/MurdleSolver](./android-app/MurdleSolver) directory as a project, and building the app through there.

**Note:** You will need to update the API hostname with the Cloud Run service that you created when deploying the ADK app. This can be done by updating `base_api_url` in `res/values/strings.xml`.

Because the app relies on camera hardware, it's probably easiest to [test with a physical device](https://developer.android.com/studio/run/device) if you have one available.

## Support, Maintenance, etc.
This app was just a fun weekend project and an excuse to play around with ADK. It's not likely that I will be maintaining, updating etc. this repo, but you never know!

See [./LICENSE.md](./LICENSE.md) for information regarding permissive use and warranty.