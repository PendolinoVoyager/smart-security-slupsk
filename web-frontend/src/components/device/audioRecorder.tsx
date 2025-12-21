"use client"

import { useState, useRef } from 'react';
import { Mic, MicOff } from 'lucide-react';
import { ENDPOINTS } from '@/api/config';

export default function AudioRecorder({deviceId, token}: {deviceId: number, token: string}) {
  const [isRecording, setIsRecording] = useState(false);
  const [status, setStatus] = useState('Ready');
  const mediaRecorderRef = useRef<MediaRecorder>(null);
  const wsRef = useRef<WebSocket>(null);

  const startRecording = async () => {
    try {
      setStatus('Connecting...');
      
      // Connect to WebSocket server
      const ws = new WebSocket(`${ENDPOINTS.AUDIO.CONNECT}?token=${token}&deviceId=${deviceId}`);
      wsRef.current = ws;

      ws.onopen = async () => {
      
        // Get microphone access
        const stream = await navigator.mediaDevices.getUserMedia({ 
          audio: {
            echoCancellation: true,
            noiseSuppression: true,
            sampleRate: 48000
          } 
        });

        // Create MediaRecorder with Opus codec
        const mediaRecorder = new MediaRecorder(stream, {
          mimeType: 'audio/webm;codecs=opus',
          audioBitsPerSecond: 128000
        });
        
        mediaRecorderRef.current = mediaRecorder;

        // Send audio chunks as they're available
        mediaRecorder.ondataavailable = (event) => {
          if (event.data.size > 0 && ws.readyState === WebSocket.OPEN) {
            ws.send(event.data);
          }
        };

        // Start recording with chunks every 100ms
        mediaRecorder.start(100);
        setIsRecording(true);
        setStatus('Transmitting...');
      };

      ws.onerror = () => {
        stopRecording();
        setStatus('Connection error');
      };

      ws.onclose = () => {
        if (isRecording) {
          stopRecording();
          setStatus('Connection lost');
        }
      };

    } catch (error: any) {
      console.error('Error starting recording:', error);
      setStatus(`Error: ${error.message ?? "unknown error"}`);
    }
  };

  const stopRecording = () => {
      if (mediaRecorderRef.current) {
        mediaRecorderRef.current.stop();
      
      // Stop all tracks
      mediaRecorderRef.current.stream.getTracks().forEach(track => track.stop());
    }

    if (wsRef.current) {
      wsRef.current.close();
    }

    setIsRecording(false);
    setStatus('Stopped');
  };

  const toggleRecording = () => {
    if (isRecording) {
      stopRecording();
    } else {
      startRecording();
    }
  };

  return (
    <div className="flex flex-col items-center justify-center p-8">
      <div className="bg-white rounded-2xl shadow-2xl p-8 max-w-md">
        <h1 className="text-xl font-bold text-center mb-8 text-slate-800">
          Speak via your device
        </h1>
        
        <div className="flex flex-col items-center gap-6">
          <button
            onClick={toggleRecording}
            className={`w-1rem h-1rem rounded-full flex items-center justify-center transition-all duration-300 shadow-lg ${
              isRecording
                ? 'bg-red-500 hover:bg-red-600 animate-pulse'
                : 'bg-blue-500 hover:bg-blue-600'
            }`}
          >
            {isRecording ? (
              <MicOff className="w-16 h-16 text-white" />
            ) : (
              <Mic className="w-16 h-16 text-white" />
            )}
          </button>

          <div className="text-center">
            <p className="text-sm font-medium text-slate-500 mb-1">Status</p>
            <p className={`text-lg font-semibold ${
              isRecording ? 'text-red-500' : 'text-slate-700'
            }`}>
              {status}
            </p>
          </div>

          <div className="w-full bg-slate-100 rounded-lg p-4 text-sm text-slate-600">
            <p className="font-medium mb-2">Instructions:</p>
            <ul className="list-disc list-inside space-y-1">
              <li>Click button and start speaking</li>
              <li>Click again to stop</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}