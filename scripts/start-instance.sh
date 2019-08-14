#!/bin/bash
export port_number=25565
export build_number=$1
export node_number=1
teamcity_build_branch=$2

if [ ! "$(docker stack ls | grep -q ldtteam-testserver-$teamcity_build_branch)" ]
then
  candidatePorts=$(seq 25565 1 25585)
  stacks=$(docker stack ls --format '{{.Name}}' | grep 'ldtteam-testserver-')
  for stack in "${stacks[@]}"
  do
    servicesInStack=$(docker stack services "$stack")
    for serviceInStack in "${servicesInStack[@]}"
    do
      portsInUse=$(docker inspect $serviceInStack | jq ".[].Endpoint.Ports" | jq ".[].PublishedPort")
      for candidatePortIndex in "${!candidatePorts[@]}"
      do
        for inUsePortIndex in "${!portsInUse[@]}"
        do
          if [[ ${candidatePorts[candidatePortIndex]} = "${portsInUse[inUsePortIndex]}" ]]
          then
            unset 'candidatePorts[candidatePortIndex]'
          fi
        done
      done
    done
  done

  new_candidatePorts=()
  for i in "${!candidatePorts[@]}"
  do
    new_candidatePorts+=("${candidatePorts[i]}")
  done
  candidatePorts=("${new_candidatePorts[@]}")
  unset new_candidatePorts

  if [ "${#candidatePorts[@]}" -eq "0" ]
  then
    echo "Failed to determine port to use. No port available"
    exit 2
  fi

  export port_number=candidatePorts[0]

  echo "Creating new service stack for PR: $teamcity_build_branch"
else
        server=$(docker stack services ldtteam-testserver-$teamcity_build_branch --format '{{.Name}} {{.ID}}' | grep minecraft-server)
        id=${server: -12}
        export port_number=$(docker inspect $id | jq ".[].Spec.Labels.\"port\"")
        export node_number=$(docker inspect $id | jq ".[].Spec.Labels.\"node.minecraft\"")
fi

docker stack deploy -c scripts/docker-compose.yml ldtteam-testserver-$teamcity_build_branch
