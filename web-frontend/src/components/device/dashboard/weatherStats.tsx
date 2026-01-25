"use server";

import { fetchMeasurementsByDevicePaginated } from "@/api/measurements";
import { HttpError } from "@/api/utils";
import { getAuthData } from "@/lib/auth/server";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Thermometer, Droplets } from "lucide-react";

function NoMeasurements() {
  return (
    <Card className="p-6 text-center">
      <CardHeader>
        <CardTitle className="text-xl font-semibold">
          No data available
        </CardTitle>
      </CardHeader>
      <CardContent>
        <p className="text-muted-foreground">
          No weather data yet. Once your device sends measurements, you'll see
          them here.
        </p>
      </CardContent>
    </Card>
  );
}

export async function WeatherStats({ deviceId }: { deviceId: number }) {
  const authData = await getAuthData();
  if (!authData) {
    return <NoMeasurements />;
  }

  const measurements = await fetchMeasurementsByDevicePaginated(
    authData.token,
    deviceId
  );
  console.log()
  if (measurements instanceof HttpError || measurements.empty) {
    return <NoMeasurements />;
  }

  
  const temp = measurements.content.find(
    (m) => m.measurementType == "t" || m.measurementType == "temperature"
  )?.value;
  const humidity = measurements.content.find(
    (m) => m.measurementType == "h" || m.measurementType == "humidity"
  )?.value;
  console.log(temp, humidity);
  if (temp == undefined || humidity == undefined) {
    return <NoMeasurements />;
  }
  return (
    <div className="flex gap-4" style={{ "flex": "1 1 0px", width: 0 }}>
      <Card className="p-4">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-lg font-medium">Temperature</CardTitle>
          <Thermometer className="h-5 w-5" />
        </CardHeader>
        <CardContent>
          <p className="text-3xl font-bold">{temp}°C</p>
        </CardContent>
      </Card>

      <Card className="p-4 ">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-lg font-medium">Humidity</CardTitle>
          <Droplets className="h-5 w-5" />
        </CardHeader>
        <CardContent>
          <p className="text-3xl font-bold">{humidity}%</p>
        </CardContent>
      </Card>
    </div>
  );
}
